package org.synyx.matrix.bot.domain.event;

import java.util.Objects;
import java.util.Optional;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixUserId;

public class MatrixRoomInviteEvent {

  /** The room the invite is for. */
  private final MatrixRoomId roomId;

  /** The user that created the invite. */
  private final MatrixUserId invitedById;

  private MatrixRoomInviteEvent(MatrixRoomId roomId, MatrixUserId invitedById) {

    this.roomId = roomId;
    this.invitedById = invitedById;
  }

  public static Optional<MatrixRoomInviteEvent> create(
      MatrixRoomId roomId, MatrixUserId invitedById) {

    if (roomId == null || invitedById == null) {
      return Optional.empty();
    }

    return Optional.of(new MatrixRoomInviteEvent(roomId, invitedById));
  }

  public MatrixRoomId getRoomId() {
    return roomId;
  }

  public MatrixUserId getInvitedById() {
    return invitedById;
  }

  @Override
  public boolean equals(Object o) {

    if (o == null || getClass() != o.getClass()) return false;
    MatrixRoomInviteEvent that = (MatrixRoomInviteEvent) o;
    return Objects.equals(roomId, that.roomId) && Objects.equals(invitedById, that.invitedById);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roomId, invitedById);
  }

  @Override
  public String toString() {

    return "MatrixRoomInviteEvent{" + "roomId=" + roomId + ", invitedById=" + invitedById + '}';
  }
}
