package org.synyx.matrix.bot.internal.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.synyx.matrix.bot.MatrixCommunicationException;
import org.synyx.matrix.bot.domain.MatrixUserId;
import org.synyx.matrix.bot.internal.MatrixAuthentication;
import org.synyx.matrix.bot.internal.api.dto.EventIdResponseDto;
import org.synyx.matrix.bot.internal.api.dto.MatrixIdentifierDto;
import org.synyx.matrix.bot.internal.api.dto.MatrixLoginDto;
import org.synyx.matrix.bot.internal.api.dto.MatrixLoginResponseDto;
import org.synyx.matrix.bot.internal.api.dto.RoomJoinPayloadDto;
import org.synyx.matrix.bot.internal.api.dto.RoomLeavePayloadDto;
import org.synyx.matrix.bot.internal.api.dto.SyncResponseDto;

public class MatrixApi {

  private static final Duration SYNC_TIMEOUT = Duration.of(30, ChronoUnit.SECONDS);
  private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.of(30, ChronoUnit.SECONDS);
  private static final Duration SYNC_REQUEST_TIMEOUT =
      Duration.of((long) (SYNC_TIMEOUT.toMillis() * 1.5D), ChronoUnit.MILLIS);

  private final URI baseUri;
  private final MatrixAuthentication authentication;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  public MatrixApi(String url, MatrixAuthentication authentication, ObjectMapper objectMapper) {

    try {
      this.baseUri = new URI(url);
    } catch (URISyntaxException e) {
      throw new MatrixCommunicationException("Invalid matrix URI", e);
    }
    this.authentication = authentication;
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = objectMapper;
  }

  public void terminateOpenConnections() {

    httpClient.shutdownNow();
  }

  public void login() throws IOException, InterruptedException, MatrixApiException {

    final var response =
        httpClient.send(
            post(
                    "/_matrix/client/v3/login",
                    null,
                    new MatrixLoginDto(
                        new MatrixIdentifierDto("m.id.user", authentication.getUsername()),
                        authentication.getPassword(),
                        "m.login.password"))
                .build(),
            jsonBodyHandler(MatrixLoginResponseDto.class));

    expected2xx("login", response);

    final var body = response.body();
    if (body == null) {
      throw new MatrixApiException("Received no login data", response);
    }

    final var userId = MatrixUserId.from(body.userId()).orElseThrow(IllegalStateException::new);
    authentication.setUserId(userId);
    authentication.setBearerToken(body.accessToken());
  }

  public Optional<SyncResponseDto> sync(String since)
      throws IOException, InterruptedException, MatrixApiException {

    final var response =
        httpClient.send(
            get(
                    "/_matrix/client/v3/sync",
                    "timeout=%d&since=%s"
                        .formatted(
                            SYNC_TIMEOUT.toMillis(),
                            URLEncoder.encode(since, StandardCharsets.UTF_8)))
                .timeout(SYNC_REQUEST_TIMEOUT)
                .build(),
            jsonBodyHandler(SyncResponseDto.class));

    expected2xx("syncing", response);

    return Optional.ofNullable(response.body());
  }

  public Optional<SyncResponseDto> syncFull()
      throws IOException, InterruptedException, MatrixApiException {

    final var response =
        httpClient.send(
            get("/_matrix/client/v3/sync", "timeout=0").build(),
            jsonBodyHandler(SyncResponseDto.class));

    expected2xx("full syncing", response);

    return Optional.ofNullable(response.body());
  }

  public String sendEvent(String roomId, String eventType, Object event)
      throws IOException, InterruptedException, MatrixApiException {

    final var uri =
        "/_matrix/client/v3/rooms/%s/send/%s/%s".formatted(roomId, eventType, UUID.randomUUID());

    final var response =
        httpClient.send(put(uri, null, event).build(), jsonBodyHandler(EventIdResponseDto.class));

    expected2xx("sending event", response);

    return response.body().eventId();
  }

  public void joinRoom(String roomId, String reason)
      throws IOException, InterruptedException, MatrixApiException {

    final var uri = "/_matrix/client/v3/rooms/%s/join".formatted(roomId);
    final var response =
        httpClient.send(
            post(uri, null, new RoomJoinPayloadDto(reason)).build(),
            HttpResponse.BodyHandlers.ofString());

    expected2xx("joining room", response);
  }

  public void leaveRoom(String roomId, String reason)
      throws IOException, InterruptedException, MatrixApiException {

    final var uri = "/_matrix/client/v3/rooms/%s/leave".formatted(roomId);
    final var response =
        httpClient.send(
            post(uri, null, new RoomLeavePayloadDto(reason)).build(),
            HttpResponse.BodyHandlers.ofString());

    expected2xx("leaving room", response);
  }

  private HttpRequest.Builder get(String url, String query) {

    return request(url, query).GET();
  }

  private <T> HttpRequest.Builder put(String url, String query, T body) {

    try {
      return request(url, query)
          .header("Content-Type", "application/json")
          .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
    } catch (JsonProcessingException e) {
      throw new MatrixCommunicationException("Failed to parse JSON", e);
    }
  }

  private <T> HttpRequest.Builder post(String url, String query, T body) {

    try {
      return request(url, query)
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
    } catch (JsonProcessingException e) {
      throw new MatrixCommunicationException("Failed to parse JSON", e);
    }
  }

  private HttpRequest.Builder request(String url, String query) {

    HttpRequest.Builder builder;
    try {
      builder =
          HttpRequest.newBuilder(
                  new URI(
                      baseUri.getScheme(),
                      null,
                      baseUri.getHost(),
                      baseUri.getPort(),
                      url,
                      query,
                      null))
              .timeout(DEFAULT_REQUEST_TIMEOUT);
    } catch (URISyntaxException e) {
      throw new MatrixCommunicationException("Invalid URI when trying to make API request", e);
    }

    authentication
        .getBearerToken()
        .ifPresent(token -> builder.header("Authorization", "Bearer %s".formatted(token)));

    return builder;
  }

  private <T> HttpResponse.BodyHandler<T> jsonBodyHandler(Class<T> clazz) {

    return responseInfo ->
        HttpResponse.BodySubscribers.mapping(
            HttpResponse.BodySubscribers.ofByteArray(),
            bytes -> {
              try {
                return objectMapper.readValue(bytes, clazz);
              } catch (IOException e) {
                throw new MatrixCommunicationException(
                    "Invalid URI when trying to make API request", e);
              }
            });
  }

  private void expected2xx(String performedAction, HttpResponse<?> response)
      throws MatrixApiException {

    final var statusCode = response.statusCode();
    if (statusCode < 200 || statusCode >= 300) {
      throw new MatrixApiException(performedAction, response);
    }
  }
}
