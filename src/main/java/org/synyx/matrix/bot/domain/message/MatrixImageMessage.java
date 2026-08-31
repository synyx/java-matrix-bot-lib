package org.synyx.matrix.bot.domain.message;

import java.util.Optional;
import org.synyx.matrix.bot.domain.MatrixContentUri;

public class MatrixImageMessage implements MatrixMessage {

  private final String body;
  private final String fileName;
  private final Object url;

  private MatrixImageMessage(String body, String fileName, Object url) {
    this.body = body;
    this.fileName = fileName;
    this.url = url;
  }

  public static Optional<MatrixImageMessage> create(String body, String fileName, String url) {

    if (body == null) {
      return Optional.empty();
    }

    return Optional.of(
        new MatrixImageMessage(
            body, fileName, MatrixContentUri.from(url).map(Object.class::cast).orElse(url)));
  }

  public static Optional<MatrixImageMessage> create(
      String body, String fileName, MatrixContentUri url) {

    if (body == null) {
      return Optional.empty();
    }

    return Optional.of(new MatrixImageMessage(body, fileName, url));
  }

  @Override
  public String getBody() {
    return body;
  }

  public Optional<String> getFileName() {
    return Optional.ofNullable(fileName);
  }

  public Optional<MatrixContentUri> getContentUri() {
    return url instanceof MatrixContentUri contentUri ? Optional.of(contentUri) : Optional.empty();
  }

  public Optional<String> getUrl() {
    return url instanceof MatrixContentUri contentUri
        ? Optional.of(contentUri.getFormatted())
        : Optional.ofNullable((String) url);
  }

  @Override
  public MatrixMessageType getType() {
    return MatrixMessageType.IMAGE;
  }
}
