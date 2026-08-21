package org.synyx.matrix.bot.domain.message;

public interface MatrixMessage {

  MatrixMessageType getType();

  String getBody();
}
