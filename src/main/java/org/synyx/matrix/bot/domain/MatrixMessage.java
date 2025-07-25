package org.synyx.matrix.bot.domain;

public interface MatrixMessage {

    MatrixEventId getEventId();

    String getBody();

    MatrixUserId getSender();

    MatrixMessageType getType();
}
