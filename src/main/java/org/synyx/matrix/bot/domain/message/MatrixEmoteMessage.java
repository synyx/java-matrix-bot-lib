package org.synyx.matrix.bot.domain.message;

import java.util.Optional;

public class MatrixEmoteMessage implements MatrixMessage {

  private final String body;

  private MatrixEmoteMessage(String body) {
    this.body = body;
  }

  public static Optional<MatrixEmoteMessage> create(String body) {

    if (body == null) {
      return Optional.empty();
    }

    return Optional.of(new MatrixEmoteMessage(body));
  }

  @Override
  public String getBody() {
    return body;
  }

  @Override
  public MatrixMessageType getType() {
    return MatrixMessageType.EMOTE;
  }
}
