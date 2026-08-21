package org.synyx.matrix.bot.domain.event;

import java.util.Objects;
import java.util.Optional;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixUserId;

public class MatrixUserJoinRoomEvent {

  /** The room that was joined. */
  private final MatrixRoomId roomId;

  /** The user joining the room. */
  private final MatrixUserId userId;

  private MatrixUserJoinRoomEvent(MatrixRoomId roomId, MatrixUserId userId) {

    this.roomId = roomId;
    this.userId = userId;
  }

  public static Optional<MatrixUserJoinRoomEvent> create(MatrixRoomId roomId, MatrixUserId userId) {

    if (roomId == null || userId == null) {
      return Optional.empty();
    }

    return Optional.of(new MatrixUserJoinRoomEvent(roomId, userId));
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
    MatrixUserJoinRoomEvent that = (MatrixUserJoinRoomEvent) o;
    return Objects.equals(roomId, that.roomId) && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roomId, userId);
  }

  @Override
  public String toString() {
    return "MatrixUserJoinRoomEvent{" + "roomId=" + roomId + ", userId=" + userId + '}';
  }
}
