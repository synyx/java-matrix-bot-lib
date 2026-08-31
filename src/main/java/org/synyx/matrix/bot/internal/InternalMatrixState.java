package org.synyx.matrix.bot.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.synyx.matrix.bot.domain.MatrixRoomAlias;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixUserId;
import org.synyx.matrix.bot.domain.state.MatrixRoom;
import org.synyx.matrix.bot.domain.state.MatrixState;
import org.synyx.matrix.bot.internal.state.InternalMatrixRoomState;

public class InternalMatrixState implements MatrixState {

  private final MatrixUserId ownUserId;
  private final List<InternalMatrixRoomState> invitedRooms;
  private final List<InternalMatrixRoomState> joinedRooms;

  public InternalMatrixState(MatrixUserId ownUserId) {

    this.ownUserId = ownUserId;
    this.invitedRooms = new ArrayList<>();
    this.joinedRooms = new ArrayList<>();
  }

  @Override
  public MatrixUserId getOwnUserId() {
    return ownUserId;
  }

  @Override
  public List<MatrixRoom> getInvitedRooms() {
    return Collections.unmodifiableList(invitedRooms);
  }

  @Override
  public Optional<MatrixRoom> findInvitedRoom(MatrixRoomId roomId) {

    return invitedRooms.stream()
        .filter(room -> room.getId().equals(roomId))
        .map(MatrixRoom.class::cast)
        .findFirst();
  }

  protected List<InternalMatrixRoomState> getInvitedRoomsInternal() {
    return invitedRooms;
  }

  @Override
  public List<MatrixRoom> getJoinedRooms() {
    return Collections.unmodifiableList(joinedRooms);
  }

  protected List<InternalMatrixRoomState> getJoinedRoomsInternal() {
    return joinedRooms;
  }

  @Override
  public Optional<MatrixRoom> findJoinedRoom(MatrixRoomId roomId) {

    return joinedRooms.stream()
        .filter(room -> room.getId().equals(roomId))
        .map(MatrixRoom.class::cast)
        .findFirst();
  }

  @Override
  public Optional<MatrixRoom> findJoinedRoom(MatrixRoomAlias alias) {

    return joinedRooms.stream()
        .filter(
            room ->
                room.getCanonicalAlias()
                    .map(matrixRoomAlias -> matrixRoomAlias.equals(alias))
                    .orElse(false))
        .map(MatrixRoom.class::cast)
        .findFirst();
  }
}
