package org.synyx.matrix.bot.domain.message;

import java.util.Optional;

public class MatrixTextMessage implements MatrixMessage {

  private final String body;

  private MatrixTextMessage(String body) {
    this.body = body;
  }

  public static Optional<MatrixTextMessage> create(String body) {

    if (body == null) {
      return Optional.empty();
    }

    return Optional.of(new MatrixTextMessage(body));
  }

  @Override
  public String getBody() {
    return body;
  }

  @Override
  public MatrixMessageType getType() {
    return MatrixMessageType.TEXT;
  }
}
