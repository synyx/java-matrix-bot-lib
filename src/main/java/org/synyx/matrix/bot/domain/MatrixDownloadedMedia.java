package org.synyx.matrix.bot.domain;

import java.util.Objects;
import java.util.Optional;

public class MatrixDownloadedMedia {

  private final String contentType;
  private final String fileName;
  private final byte[] data;

  private MatrixDownloadedMedia(String contentType, String fileName, byte[] data) {

    this.contentType = contentType;
    this.fileName = fileName;
    this.data = data;
  }

  public static Optional<MatrixDownloadedMedia> create(
      String contentType, String fileName, byte[] data) {

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
