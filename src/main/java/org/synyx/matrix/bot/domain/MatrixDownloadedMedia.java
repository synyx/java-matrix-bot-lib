package org.synyx.matrix.bot.domain;

import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

public class MatrixDownloadedMedia {

  private final String contentType;
  @Nullable private final String fileName;
  private final byte[] data;

  private MatrixDownloadedMedia(String contentType, @Nullable String fileName, byte[] data) {

    this.contentType = contentType;
    this.fileName = fileName;
    this.data = data;
  }

  public static Optional<MatrixDownloadedMedia> create(
      @Nullable String contentType, @Nullable String fileName, byte @Nullable [] data) {

    contentType = Objects.requireNonNullElse(contentType, "application/octet-stream");
    data = Objects.requireNonNullElseGet(data, () -> new byte[0]);

    return Optional.of(new MatrixDownloadedMedia(contentType, fileName, data));
  }

  public String getContentType() {
    return contentType;
  }

  public Optional<String> getFileName() {
    return Optional.ofNullable(fileName);
  }

  public byte[] getData() {
    return data;
  }
}
