package org.synyx.matrix.bot.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class MatrixContentUri {

  /*
  Content URIs follow a specific format:
  https://spec.matrix.org/v1.18/client-server-api/#matrix-content-mxc-uris
   */
  private static final Pattern CONTENT_URI_PATTERN = Pattern.compile("^mxc://([^/]+)/(.+)$");

  private final String serverName;
  private final String mediaId;

  private MatrixContentUri(String serverName, String mediaId) {

    this.serverName = serverName;
    this.mediaId = mediaId;
  }

  public static Optional<MatrixContentUri> from(String value) {

    final var matcher = CONTENT_URI_PATTERN.matcher(value);
    if (!matcher.matches()) {
      return Optional.empty();
    }

    final var serverName = matcher.group(1);
    final var mediaId = matcher.group(2);

    return Optional.of(new MatrixContentUri(serverName, mediaId));
  }

  public String getServerName() {
    return serverName;
  }

  public String getMediaId() {
    return mediaId;
  }

  public String getFormatted() {
    return "mxc://%s/%s".formatted(serverName, mediaId);
  }

  @Override
  public boolean equals(Object o) {

    if (o == null || getClass() != o.getClass()) return false;
    MatrixContentUri that = (MatrixContentUri) o;
    return Objects.equals(serverName, that.serverName) && Objects.equals(mediaId, that.mediaId);
  }

  @Override
  public int hashCode() {

    return Objects.hash(serverName, mediaId);
  }

  @Override
  public String toString() {

    return getFormatted();
  }
}
