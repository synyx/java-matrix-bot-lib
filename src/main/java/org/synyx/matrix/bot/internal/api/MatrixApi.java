package org.synyx.matrix.bot.internal.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.synyx.matrix.bot.domain.MatrixUserId;
import org.synyx.matrix.bot.internal.MatrixAuthentication;
import org.synyx.matrix.bot.internal.api.dto.EventIdResponseDto;
import org.synyx.matrix.bot.internal.api.dto.MatrixIdentifierDto;
import org.synyx.matrix.bot.internal.api.dto.MatrixLoginDto;
import org.synyx.matrix.bot.internal.api.dto.MatrixLoginResponseDto;
import org.synyx.matrix.bot.internal.api.dto.RoomJoinPayloadDto;
import org.synyx.matrix.bot.internal.api.dto.RoomLeavePayloadDto;
import org.synyx.matrix.bot.internal.api.dto.SyncResponseDto;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class MatrixApi {

  private static final int SYNC_TIMEOUT = 30_000;

  private final URI baseUri;
  private final MatrixAuthentication authentication;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  public MatrixApi(String hostname, MatrixAuthentication authentication, ObjectMapper objectMapper) {

    try {
      this.baseUri = new URI(hostname);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    this.authentication = authentication;
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = objectMapper;
  }

  public void terminateOpenConnections() {

    httpClient.shutdownNow();
  }

  public boolean login() throws IOException, InterruptedException {

    final var response = httpClient.send(
        post(
            "/_matrix/client/v3/login",
            null,
            new MatrixLoginDto(
                new MatrixIdentifierDto("m.id.user", authentication.getUsername()),
                authentication.getPassword(),
                "m.login.password"
            )
        ).build(),
        jsonBodyHandler(MatrixLoginResponseDto.class)
    );

    final var maybeBody = Optional.ofNullable(response.body());
    maybeBody.ifPresent(body -> {
      final var userId = MatrixUserId.from(body.userId())
          .orElseThrow(IllegalStateException::new);
      authentication.setUserId(userId);
      authentication.setBearerToken(body.accessToken());
    });

    return maybeBody.isPresent();
  }

  public Optional<SyncResponseDto> sync(String since) throws InterruptedException {

    try {
      final var response = httpClient.send(
          get(
              "/_matrix/client/v3/sync",
              "timeout=%d&since=%s".formatted(
                  SYNC_TIMEOUT,
                  URLEncoder.encode(since, StandardCharsets.UTF_8)
              )
          ).build(),
          jsonBodyHandler(SyncResponseDto.class)
      );

      return Optional.ofNullable(response.body());
    } catch (IOException e) {
      log.error("Failed to sync", e);
    }

    return Optional.empty();
  }

  public Optional<SyncResponseDto> syncFull() throws InterruptedException {

    try {
      final var response = httpClient.send(
          get("/_matrix/client/v3/sync", "timeout=0").build(),
          jsonBodyHandler(SyncResponseDto.class)
      );

      return Optional.ofNullable(response.body());
    } catch (IOException e) {
      log.error("Failed to sync", e);
    }

    return Optional.empty();
  }

  public boolean sendEvent(String roomId, String eventType, Object event) throws InterruptedException {

    final var uri = "/_matrix/client/v3/rooms/%s/send/%s/%s".formatted(
        roomId,
        eventType,
        UUID.randomUUID()
    );
    try {
      final var response = httpClient.send(
          put(uri, null, event).build(),
          jsonBodyHandler(EventIdResponseDto.class)
      );
      return response.statusCode() >= 200 && response.statusCode() < 300;
    } catch (IOException e) {
      log.error("Failed to send event", e);
    }

    return false;
  }

  public boolean joinRoom(String roomId, String reason) throws InterruptedException {

    final var uri = "/_matrix/client/v3/rooms/%s/join".formatted(roomId);
    try {
      final var response = httpClient.send(
          post(uri, null, new RoomJoinPayloadDto(reason)).build(),
          HttpResponse.BodyHandlers.ofString()
      );
      return response.statusCode() >= 200 && response.statusCode() < 300;
    } catch (IOException e) {
      log.error("Failed to join room", e);
    }

    return false;
  }

  public boolean leaveRoom(String roomId, String reason) throws InterruptedException {

    final var uri = "/_matrix/client/v3/rooms/%s/leave".formatted(roomId);
    try {
      final var response = httpClient.send(
          post(uri, null, new RoomLeavePayloadDto(reason)).build(),
          HttpResponse.BodyHandlers.ofString()
      );
      return response.statusCode() >= 200 && response.statusCode() < 300;
    } catch (IOException e) {
      log.error("Failed to leave room", e);
    }

    return false;
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
      throw new RuntimeException(e);
    }
  }

  private <T> HttpRequest.Builder post(String url, String query, T body) {

    try {
      return request(url, query)
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private HttpRequest.Builder request(String url, String query) {

    HttpRequest.Builder builder;
    try {
      builder = HttpRequest.newBuilder(new URI(baseUri.getScheme(), null, baseUri.getHost(), baseUri.getPort(), url, query, null));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    authentication.getBearerToken().ifPresent(token -> builder.header("Authorization", "Bearer %s".formatted(token)));

    return builder;
  }

  private <T> HttpResponse.BodyHandler<T> jsonBodyHandler(Class<T> clazz) {

    return responseInfo -> HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofByteArray(), bytes -> {
          try {
            log.debug("sync: {}", new String(bytes, StandardCharsets.UTF_8));
            return objectMapper.readValue(bytes, clazz);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
    );
  }
}
