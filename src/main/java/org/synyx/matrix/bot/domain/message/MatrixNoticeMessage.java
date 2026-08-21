package org.synyx.matrix.bot.domain.message;

import java.util.Optional;

public class MatrixNoticeMessage implements MatrixMessage {

  private final String body;

  private MatrixNoticeMessage(String body) {
    this.body = body;
  }

  public static Optional<MatrixNoticeMessage> create(String body) {

    if (body == null) {
      return Optional.empty();
    }

    return Optional.of(new MatrixNoticeMessage(body));
  }

  @Override
  public String getBody() {
    return body;
  }

  @Override
  public MatrixMessageType getType() {
    return MatrixMessageType.NOTICE;
  }
}
