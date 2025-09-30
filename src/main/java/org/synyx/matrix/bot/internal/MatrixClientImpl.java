package org.synyx.matrix.bot.internal;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.synyx.matrix.bot.MatrixClient;
import org.synyx.matrix.bot.MatrixCommunicationException;
import org.synyx.matrix.bot.MatrixEventConsumer;
import org.synyx.matrix.bot.MatrixPersistedStateProvider;
import org.synyx.matrix.bot.MatrixState;
import org.synyx.matrix.bot.domain.MatrixEventId;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixUserId;
import org.synyx.matrix.bot.internal.api.MatrixApi;
import org.synyx.matrix.bot.internal.api.MatrixApiException;
import org.synyx.matrix.bot.internal.api.dto.MessageDto;
import org.synyx.matrix.bot.internal.api.dto.ReactionDto;
import org.synyx.matrix.bot.internal.api.dto.ReactionRelatesToDto;
import org.synyx.matrix.bot.internal.api.dto.SyncResponseDto;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class MatrixClientImpl implements MatrixClient {

  private static final long DEFAULT_BACKOFF_IN_SEC = 3;
  private static final long BACKOFF_MAX_IN_SEC = 60;

  private final MatrixAuthentication authentication;
  private final ObjectMapper objectMapper;
  private final MatrixApi api;
  private MatrixState state;
  private MatrixStateSynchronizer stateSynchronizer;
  private MatrixPersistedStateProvider persistedState;
  private MatrixEventNotifier eventNotifier;
  private boolean interruptionRequested;
  private long currentBackoffInSec;

  public MatrixClientImpl(String url, String username, String password) {

    this.authentication = new MatrixAuthentication(username, password);
    this.objectMapper = JsonMapper.builder()
        .addModule(new Jdk8Module())
        .addModule(new JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
        .build();
    this.api = new MatrixApi(url, authentication, objectMapper);
    this.state = null;
    this.eventNotifier = null;
    this.interruptionRequested = false;
    this.currentBackoffInSec = DEFAULT_BACKOFF_IN_SEC;
  }

  @Override
  public void setEventCallback(MatrixEventConsumer eventConsumer) {

    this.eventNotifier = MatrixEventNotifier.from(objectMapper, eventConsumer).orElse(null);
  }

  @Override
  public void setPersistedStateProvider(MatrixPersistedStateProvider persistedState) {

    this.persistedState = persistedState;
  }

  @Override
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

        clearSyncState();
        Thread.sleep(currentBackoffInSec * 1000);
        currentBackoffInSec = Math.min(currentBackoffInSec * 2, BACKOFF_MAX_IN_SEC);
      }
    }

    clearSyncState();
    interruptionRequested = false;
    currentBackoffInSec = DEFAULT_BACKOFF_IN_SEC;
  }

  @Override
  public void requestStopOfSync() {

    interruptionRequested = true;
    api.terminateOpenConnections();
  }

  private void clearSyncState() {

    authentication.clear();
    state = null;
  }

  @Override
  public boolean isConnected() {

    return state != null;
  }

  @Override
  public Optional<MatrixState> getState() {

    return Optional.ofNullable(state);
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
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
