package org.synyx.matrix.bot;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.synyx.matrix.bot.domain.MatrixEventId;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixUserId;
import org.synyx.matrix.bot.internal.MatrixAuthentication;
import org.synyx.matrix.bot.internal.MatrixBackoffException;
import org.synyx.matrix.bot.internal.MatrixEventNotifier;
import org.synyx.matrix.bot.internal.MatrixStateSynchronizer;
import org.synyx.matrix.bot.internal.api.MatrixApi;
import org.synyx.matrix.bot.internal.api.MatrixApiException;
import org.synyx.matrix.bot.internal.api.dto.MessageDto;
import org.synyx.matrix.bot.internal.api.dto.ReactionDto;
import org.synyx.matrix.bot.internal.api.dto.ReactionRelatesToDto;
import org.synyx.matrix.bot.internal.api.dto.SyncResponseDto;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class MatrixClient {

  private static final long DEFAULT_BACKOFF_IN_SEC = 3;
  private static final long BACKOFF_MAX_IN_SEC = 60;

  private final MatrixAuthentication authentication;
  private final ObjectMapper objectMapper;
  private final MatrixApi api;
  private MatrixState state;
  private MatrixStateSynchronizer stateSynchronizer;
  private MatrixPersistedState persistedState;
  private MatrixEventNotifier eventNotifier;
  private boolean interruptionRequested;
  private long currentBackoffInSec;

  public MatrixClient(String hostname, String username, String password) {

    this.authentication = new MatrixAuthentication(username, password);
    this.objectMapper = JsonMapper.builder()
        .addModule(new Jdk8Module())
        .addModule(new JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
        .build();
    this.api = new MatrixApi(hostname, authentication, objectMapper);
    this.state = null;
    this.eventNotifier = null;
    this.interruptionRequested = false;
    this.currentBackoffInSec = DEFAULT_BACKOFF_IN_SEC;
  }

  public void setEventCallback(MatrixEventConsumer eventConsumer) {

    this.eventNotifier = MatrixEventNotifier.from(objectMapper, eventConsumer).orElse(null);
  }

  public void setPersistedState(MatrixPersistedState persistedState) {

    this.persistedState = persistedState;
  }

  public void requestStopOfSync() {

    interruptionRequested = true;
    api.terminateOpenConnections();
  }

  public void syncContinuous() throws InterruptedException {

    while (!interruptionRequested) {
      try {
        if (!authentication.isAuthenticated()) {
          try {
            api.login();
          } catch (IOException e) {
            throw new MatrixBackoffException("Failed to login to matrix server!", e);
          } catch (MatrixApiException e) {
            throw new MatrixCommunicationException("Failed to login to matrix server!", e);
          }

          log.info("Successfully logged in to matrix server as {}",
              authentication.getUserId()
                  .map(MatrixUserId::toString)
                  .orElse("UNKNOWN")
          );
        }

        state = new MatrixState(authentication.getUserId().orElseThrow(IllegalStateException::new));
        stateSynchronizer = new MatrixStateSynchronizer(state, objectMapper);

        SyncResponseDto syncResponse;
        try {
          syncResponse = api.syncFull()
              .orElseThrow(() -> new MatrixCommunicationException("No data in initial sync"));
        } catch (MatrixApiException | IOException e) {
          throw new MatrixBackoffException("Failed to perform initial sync", e);
        }

        String lastBatch = syncResponse.nextBatch();
        stateSynchronizer.synchronizeState(syncResponse);

        if (eventNotifier != null) {
          eventNotifier.getConsumer().onConnected(state);
        }

        if (persistedState != null) {
          final var maybePersistedLastBatch = persistedState.getLastBatch();
          if (maybePersistedLastBatch.isPresent()) {
            lastBatch = maybePersistedLastBatch.get();
          } else {
            persistedState.setLastBatch(lastBatch);
          }
        }

        while (!interruptionRequested) {
          Optional<SyncResponseDto> maybePartialSyncResponse;

          try {
            maybePartialSyncResponse = api.sync(lastBatch);
          } catch (MatrixApiException | IOException e) {
            throw new MatrixBackoffException("Could not partial sync", e);
          }

          if (maybePartialSyncResponse.isPresent()) {
            syncResponse = maybePartialSyncResponse.get();
            lastBatch = syncResponse.nextBatch();

            stateSynchronizer.synchronizeState(syncResponse);

            if (eventNotifier != null) {
              eventNotifier.notifyFromSynchronizationResponse(state, syncResponse);
            }

            if (persistedState != null) {
              persistedState.setLastBatch(lastBatch);
            }
          }

          currentBackoffInSec = DEFAULT_BACKOFF_IN_SEC;
        }

      } catch (MatrixBackoffException e) {
        log.warn("Sync failed: {}, backing off for {}s", e.getCause().getClass().getName(), currentBackoffInSec);

        Thread.sleep(currentBackoffInSec * 1000);
        authentication.clear();
        currentBackoffInSec = Math.min(currentBackoffInSec * 2, BACKOFF_MAX_IN_SEC);
      }
    }

    interruptionRequested = false;
    currentBackoffInSec = DEFAULT_BACKOFF_IN_SEC;
  }

  public boolean isConnected() {

    return state != null;
  }

  public Optional<MatrixState> getState() {

    return Optional.ofNullable(state);
  }

  public Optional<MatrixEventId> sendMessage(MatrixRoomId roomId, String messageBody) {

    try {
      return MatrixEventId.from(
          api.sendEvent(roomId.getFormatted(), "m.room.message", new MessageDto(messageBody, "m.text"))
      );
    } catch (InterruptedException | IOException e) {
      log.error("Failed to send message", e);
    } catch (MatrixApiException e) {
      log.warn("Could not send message", e);
    }

    return Optional.empty();
  }

  public Optional<MatrixEventId> addReaction(MatrixRoomId roomId, MatrixEventId eventId, String reaction) {

    final var reactionDto = new ReactionDto(new ReactionRelatesToDto(eventId.getFormatted(), reaction));
    try {
      return MatrixEventId.from(
          api.sendEvent(roomId.getFormatted(), "m.reaction", reactionDto)
      );
    } catch (InterruptedException | IOException e) {
      log.error("Failed to add reaction", e);
    } catch (MatrixApiException e) {
      log.warn("Could not add reaction", e);
    }

    return Optional.empty();
  }

  public boolean joinRoom(MatrixRoomId roomId) {

    try {
      api.joinRoom(roomId.getFormatted(), "i'm a bot");
      return true;
    } catch (InterruptedException | IOException e) {
      log.error("Failed to join room", e);
    } catch (MatrixApiException e) {
      log.warn("Could not join room", e);
    }

    return false;
  }

  public boolean leaveRoom(MatrixRoomId roomId) {

    try {
      api.leaveRoom(roomId.getFormatted(), "i'm a bot");
      return true;
    } catch (InterruptedException | IOException e) {
      log.error("Failed to leave room", e);
    } catch (MatrixApiException e) {
      log.warn("Could not leave room", e);
    }

    return false;
  }
}
