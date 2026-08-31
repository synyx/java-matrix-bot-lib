package org.synyx.matrix.bot.domain.event;

import java.util.Objects;
import java.util.Optional;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixUserId;

public class MatrixUserLeaveRoomEvent {

  /** The room that was left. */
  private final MatrixRoomId roomId;

  /** The user that left the room. */
  private final MatrixUserId userId;

  private MatrixUserLeaveRoomEvent(MatrixRoomId roomId, MatrixUserId userId) {

    this.roomId = roomId;
    this.userId = userId;
  }

  public static Optional<MatrixUserLeaveRoomEvent> create(
      MatrixRoomId roomId, MatrixUserId userId) {

    if (roomId == null || userId == null) {
      return Optional.empty();
    }

    return Optional.of(new MatrixUserLeaveRoomEvent(roomId, userId));
  }

  public MatrixRoomId getRoomId() {
    return roomId;
  }

  public MatrixUserId getUserId() {
    return userId;
  }

  @Override
  public boolean equals(Object o) {

    if (o == null || getClass() != o.getClass()) return false;
    MatrixUserLeaveRoomEvent that = (MatrixUserLeaveRoomEvent) o;
    return Objects.equals(roomId, that.roomId) && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roomId, userId);
  }

  @Override
  public String toString() {
    return "MatrixUserLeaveRoomEvent{" + "roomId=" + roomId + ", userId=" + userId + '}';
  }
}
