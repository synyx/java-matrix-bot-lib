package org.synyx.matrix.bot.domain.event;

import java.util.Objects;
import java.util.Optional;
import org.synyx.matrix.bot.domain.MatrixEventId;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixUserId;
import org.synyx.matrix.bot.domain.message.MatrixMessage;

public class MatrixMessageEvent {

  /** The ID of the message event. */
  private final MatrixEventId eventId;

  /** The room the message was received in. */
  private final MatrixRoomId roomId;

  /** The user that sent the received message. */
  private final MatrixUserId senderId;

  /** The message that was received. */
  private final MatrixMessage message;

  private MatrixMessageEvent(
      MatrixEventId eventId, MatrixRoomId roomId, MatrixUserId senderId, MatrixMessage message) {

    this.eventId = eventId;
    this.roomId = roomId;
    this.senderId = senderId;
    this.message = message;
  }

  public static Optional<MatrixMessageEvent> create(
      MatrixEventId eventId, MatrixRoomId roomId, MatrixUserId senderId, MatrixMessage message) {

    if (eventId == null || roomId == null || senderId == null || message == null) {
      return Optional.empty();
    }

    return Optional.of(new MatrixMessageEvent(eventId, roomId, senderId, message));
  }

  public MatrixEventId getEventId() {
    return eventId;
  }

  public MatrixRoomId getRoomId() {
    return roomId;
  }

  public MatrixUserId getSenderId() {
    return senderId;
  }

  public MatrixMessage getMessage() {
    return message;
  }

  @Override
  public boolean equals(Object o) {

    if (o == null || getClass() != o.getClass()) return false;
    MatrixMessageEvent that = (MatrixMessageEvent) o;
    return Objects.equals(eventId, that.eventId)
        && Objects.equals(roomId, that.roomId)
        && Objects.equals(senderId, that.senderId)
        && Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventId, roomId, senderId, message);
  }

  @Override
  public String toString() {

    return "MatrixMessageEvent{"
        + "eventId="
        + eventId
        + ", roomId="
        + roomId
        + ", senderId="
        + senderId
        + ", message="
        + message
        + '}';
  }
}
