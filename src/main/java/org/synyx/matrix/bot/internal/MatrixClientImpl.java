package org.synyx.matrix.bot.internal;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.synyx.matrix.bot.MatrixClient;
import org.synyx.matrix.bot.MatrixCommunicationException;
import org.synyx.matrix.bot.MatrixEventConsumer;
import org.synyx.matrix.bot.MatrixPersistedStateProvider;
import org.synyx.matrix.bot.domain.MatrixContentUri;
import org.synyx.matrix.bot.domain.MatrixDownloadedMedia;
import org.synyx.matrix.bot.domain.MatrixEventId;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixUserId;
import org.synyx.matrix.bot.domain.message.MatrixEmoteMessage;
import org.synyx.matrix.bot.domain.message.MatrixImageMessage;
import org.synyx.matrix.bot.domain.message.MatrixMessage;
import org.synyx.matrix.bot.domain.message.MatrixNoticeMessage;
import org.synyx.matrix.bot.domain.message.MatrixTextMessage;
import org.synyx.matrix.bot.domain.state.MatrixState;
import org.synyx.matrix.bot.internal.api.MatrixApi;
import org.synyx.matrix.bot.internal.api.MatrixApiException;
import org.synyx.matrix.bot.internal.api.dto.ImageMessageDto;
import org.synyx.matrix.bot.internal.api.dto.MessageDto;
import org.synyx.matrix.bot.internal.api.dto.ReactionDto;
import org.synyx.matrix.bot.internal.api.dto.ReactionRelatesToDto;
import org.synyx.matrix.bot.internal.api.dto.SyncResponseDto;

public class MatrixClientImpl implements MatrixClient {

  private static final Logger LOG = LoggerFactory.getLogger(MatrixClientImpl.class);

  private static final long DEFAULT_BACKOFF_IN_SEC = 3;
  private static final long BACKOFF_MAX_IN_SEC = 60;

  private final MatrixAuthentication authentication;
  private final ObjectMapper objectMapper;
  private final MatrixApi api;
  private InternalMatrixState state;
  private MatrixStateSynchronizer stateSynchronizer;
  private MatrixPersistedStateProvider persistedState;
  private MatrixEventNotifier eventNotifier;
  private boolean interruptionRequested;
  private long currentBackoffInSec;

  public MatrixClientImpl(String url, String username, String password) {

    this.authentication = new MatrixAuthentication(username, password);
    this.objectMapper =
        JsonMapper.builder()
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
            api.login().get();
          } catch (ExecutionException e) {
            final var cause = e.getCause();
            if (cause instanceof IOException) {
              throw new MatrixBackoffException("Failed to login to matrix server!", cause);
            } else if (cause instanceof MatrixApiException matrixApiException) {
              throw matrixApiException;
            } else {
              throw new MatrixCommunicationException("Failed to login to matrix server!", cause);
            }
          }

          if (LOG.isInfoEnabled()) {
            LOG.info(
                "Successfully logged in to matrix server as {}",
                authentication.getUserId().map(MatrixUserId::toString).orElse("UNKNOWN"));
          }
        }

        state =
            new InternalMatrixState(
                authentication.getUserId().orElseThrow(IllegalStateException::new));
        stateSynchronizer = new MatrixStateSynchronizer(state, objectMapper);

        SyncResponseDto syncResponse;
        try {
          syncResponse = api.syncFull().get();
        } catch (ExecutionException e) {
          final var cause = e.getCause();
          if (cause instanceof MatrixApiException matrixApiException) {
            throw new MatrixCommunicationException("No data in initial sync", e);
          }

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

          try {
            syncResponse = api.sync(lastBatch).get();
          } catch (ExecutionException e) {
            throw new MatrixBackoffException("Could not partial sync", e.getCause());
          }

          lastBatch = syncResponse.nextBatch();

          stateSynchronizer.synchronizeState(syncResponse);

          if (eventNotifier != null) {
            eventNotifier.notifyFromSynchronizationResponse(state, syncResponse);
          }

          if (persistedState != null) {
            persistedState.setLastBatch(lastBatch);
          }

          currentBackoffInSec = DEFAULT_BACKOFF_IN_SEC;
        }

      } catch (MatrixBackoffException e) {
        LOG.warn(
            "Sync failed: {}, backing off for {}s",
            e.getCause().getClass().getName(),
            currentBackoffInSec);

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
  public CompletableFuture<MatrixDownloadedMedia> downloadMedia(MatrixContentUri contentUri) {

    return api.downloadMedia(contentUri.getServerName(), contentUri.getMediaId())
        .exceptionally(
            e -> {
              LOG.error("Failed to download media", e);
              throw new RuntimeException(e);
            });
  }

  @Override
  public CompletableFuture<MatrixContentUri> uploadMedia(
      byte[] data, String contentType, String fileName) {

    return api.uploadMedia(contentType, fileName, data)
        .thenApply(
            value ->
                MatrixContentUri.from(value)
                    .orElseThrow(
                        () ->
                            new IllegalStateException(
                                "Not a valid matrix content uri: %s".formatted(value))))
        .exceptionally(
            e -> {
              LOG.error("Failed to upload media", e);
              throw new RuntimeException(e);
            });
  }

  @Override
  public CompletableFuture<MatrixEventId> sendMessage(MatrixRoomId roomId, String messageBody) {

    final var maybeTextMessage = MatrixTextMessage.create(messageBody);
    if (maybeTextMessage.isEmpty()) {
      return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid messageBody"));
    }

    return sendMessage(roomId, maybeTextMessage.get());
  }

  @Override
  public CompletableFuture<MatrixEventId> sendMessage(MatrixRoomId roomId, MatrixMessage message) {

    final Optional<?> eventDto =
        switch (message) {
          case MatrixTextMessage textMessage ->
              Optional.of(new MessageDto(textMessage.getBody(), "m.text"));
          case MatrixEmoteMessage emoteMessage ->
              Optional.of(new MessageDto(emoteMessage.getBody(), "m.emote"));
          case MatrixNoticeMessage noticeMessage ->
              Optional.of(new MessageDto(noticeMessage.getBody(), "m.notice"));
          case MatrixImageMessage imageMessage ->
              Optional.of(
                  new ImageMessageDto(
                      imageMessage.getBody(),
                      "m.image",
                      imageMessage.getFileName().orElse(null),
                      imageMessage.getUrl().orElse(null)));
          default -> Optional.empty();
        };

    if (eventDto.isEmpty()) {
      return CompletableFuture.failedFuture(
          new IllegalArgumentException("Unsupported message type"));
    }

    return api.sendEvent(roomId.getFormatted(), "m.room.message", eventDto.get())
        .thenApply(
            value ->
                MatrixEventId.from(value)
                    .orElseThrow(
                        () ->
                            new IllegalStateException(
                                "Not a valid matrix event id: %s".formatted(value))))
        .exceptionally(
            e -> {
              LOG.error("Failed to send message", e);
              throw new RuntimeException(e);
            });
  }

  @Override
  public CompletableFuture<MatrixEventId> addReaction(
      MatrixRoomId roomId, MatrixEventId eventId, String reaction) {

    final var reactionDto =
        new ReactionDto(new ReactionRelatesToDto(eventId.getFormatted(), reaction));

    return api.sendEvent(roomId.getFormatted(), "m.reaction", reactionDto)
        .thenApply(
            value ->
                MatrixEventId.from(value)
                    .orElseThrow(
                        () ->
                            new IllegalStateException(
                                "Not a valid matrix event id: %s".formatted(value))))
        .exceptionally(
            e -> {
              LOG.error("Failed to add reaction", e);
              throw new RuntimeException(e);
            });
  }

  @Override
  public CompletableFuture<Void> joinRoom(MatrixRoomId roomId) {

    return api.joinRoom(roomId.getFormatted(), "i'm a bot")
        .exceptionally(
            e -> {
              LOG.error("Failed to join room", e);
              throw new RuntimeException(e);
            });
  }

  @Override
  public CompletableFuture<Void> leaveRoom(MatrixRoomId roomId) {

    return api.leaveRoom(roomId.getFormatted(), "i'm a bot")
        .exceptionally(
            e -> {
              LOG.error("Failed to leave room", e);
              throw new RuntimeException(e);
            });
  }
}
