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
import org.synyx.matrix.bot.internal.MatrixEventNotifier;
import org.synyx.matrix.bot.internal.MatrixStateSynchronizer;
import org.synyx.matrix.bot.internal.api.MatrixApi;
import org.synyx.matrix.bot.internal.api.dto.MessageDto;
import org.synyx.matrix.bot.internal.api.dto.ReactionDto;
import org.synyx.matrix.bot.internal.api.dto.ReactionRelatesToDto;

import java.util.Optional;

@Slf4j
public class MatrixClient {

  private final MatrixAuthentication authentication;
  private final ObjectMapper objectMapper;
  private final MatrixApi api;
  private MatrixState state;
  private MatrixStateSynchronizer stateSynchronizer;
  private MatrixPersistedState persistedState;
  private MatrixEventNotifier eventNotifier;
  private boolean interruptionRequested;

  public MatrixClient(String hostname, String username, String password) {

    this.authentication = new MatrixAuthentication(username, password);
    this.objectMapper = JsonMapper.builder()
        .addModule(new Jdk8Module())
        .addModule(new JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build();
    this.api = new MatrixApi(hostname, authentication, objectMapper);
    this.state = null;
    this.eventNotifier = null;
    this.interruptionRequested = false;
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

  public void syncContinuous() {

    if (!authentication.isAuthenticated()) {
      if (api.login()) {
        log.info("Successfully logged in to matrix server as {}",
            authentication.getUserId()
                .map(MatrixUserId::toString)
                .orElse("UNKNOWN")
        );
      } else {
        return;
      }
    }

    state = new MatrixState(authentication.getUserId().orElseThrow(IllegalStateException::new));
    stateSynchronizer = new MatrixStateSynchronizer(state, objectMapper);

    var maybeSyncResponse = api.syncFull();
    String lastBatch;
    if (maybeSyncResponse.isPresent()) {
      final var syncResponse = maybeSyncResponse.get();
      lastBatch = syncResponse.nextBatch();

      stateSynchronizer.synchronizeState(syncResponse);
    } else {
      log.error("Failed to perform initial sync");
      return;
    }

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
      maybeSyncResponse = api.sync(lastBatch);
      if (maybeSyncResponse.isPresent()) {
        final var syncResponse = maybeSyncResponse.get();
        lastBatch = syncResponse.nextBatch();

        stateSynchronizer.synchronizeState(syncResponse);

        if (eventNotifier != null) {
          eventNotifier.notifyFromSynchronizationResponse(state, syncResponse);
        }

        if (persistedState != null) {
          persistedState.setLastBatch(lastBatch);
        }
      }
    }

    interruptionRequested = false;
  }

  public boolean isConnected() {

    return state != null;
  }

  public Optional<MatrixState> getState() {

    return Optional.ofNullable(state);
  }

  public boolean sendMessage(MatrixRoomId roomId, String messageBody) {

    return api.sendEvent(roomId.getFormatted(), "m.room.message", new MessageDto(messageBody, "m.text"));
  }

  public boolean addReaction(MatrixRoomId roomId, MatrixEventId eventId, String reaction) {

    final var reactionDto = new ReactionDto(new ReactionRelatesToDto(eventId.getFormatted(), reaction));
    return api.sendEvent(roomId.getFormatted(), "m.reaction", reactionDto);
  }

  public boolean joinRoom(MatrixRoomId roomId) {

    return api.joinRoom(roomId.getFormatted(), "hello there");
  }

  public boolean leaveRoom(MatrixRoomId roomId) {

    return api.leaveRoom(roomId.getFormatted(), "bai");
  }
}
