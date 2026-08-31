package org.synyx.matrix.bot.domain.event;

import java.util.Objects;
import java.util.Optional;
import org.synyx.matrix.bot.domain.MatrixRoomId;

public class MatrixSelfLeaveRoomEvent {

  /** The room that was left. */
  private final MatrixRoomId roomId;

  private MatrixSelfLeaveRoomEvent(MatrixRoomId roomId) {
    this.roomId = roomId;
  }

  public static Optional<MatrixSelfLeaveRoomEvent> create(MatrixRoomId roomId) {

    if (roomId == null) {
      return Optional.empty();
    }

    return Optional.of(new MatrixSelfLeaveRoomEvent(roomId));
  }

  public MatrixRoomId getRoomId() {
    return roomId;
  }

  @Override
  public boolean equals(Object o) {

    if (o == null || getClass() != o.getClass()) return false;
    MatrixSelfLeaveRoomEvent that = (MatrixSelfLeaveRoomEvent) o;
    return Objects.equals(roomId, that.roomId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(roomId);
  }

  @Override
  public String toString() {
    return "MatrixSelfLeaveRoomEvent{" + "roomId=" + roomId + '}';
  }
}
