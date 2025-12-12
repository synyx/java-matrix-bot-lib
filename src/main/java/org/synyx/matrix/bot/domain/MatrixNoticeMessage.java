package org.synyx.matrix.bot.domain;

import java.util.Optional;

public class MatrixNoticeMessage implements MatrixMessage {

  private final MatrixEventId eventId;
  private final String body;
  private final MatrixUserId sender;

  private MatrixNoticeMessage(MatrixEventId eventId, String body, MatrixUserId sender) {

    this.eventId = eventId;
    this.body = body;
    this.sender = sender;
  }

  public static Optional<MatrixNoticeMessage> from(
      MatrixEventId eventId, String body, MatrixUserId sender) {

    if (eventId == null || body == null || sender == null) {
      return Optional.empty();
    }

    return Optional.of(new MatrixNoticeMessage(eventId, body, sender));
  }

  @Override
  public MatrixEventId getEventId() {

    return eventId;
  }

  @Override
  public String getBody() {

    return body;
  }

  @Override
  public MatrixUserId getSender() {

    return sender;
  }

  @Override
  public MatrixMessageType getType() {

    return MatrixMessageType.NOTICE;
  }
}
