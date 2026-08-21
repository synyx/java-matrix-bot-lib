package org.synyx.matrix.bot.internal.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.synyx.matrix.bot.domain.MatrixRoomAlias;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixUserId;
import org.synyx.matrix.bot.domain.state.MatrixRoom;
import org.synyx.matrix.bot.domain.state.MatrixUser;

public class InternalMatrixRoomState implements MatrixRoom {

  private final MatrixRoomId id;
  private MatrixRoomAlias canonicalAlias;
  private String name;

  private final List<InternalMatrixUserState> roomUsers;

  private InternalMatrixRoomState(MatrixRoomId id) {

    this.id = id;
    this.roomUsers = new ArrayList<>();
  }

  public static Optional<InternalMatrixRoomState> from(MatrixRoomId id) {

    if (id == null) {
      return Optional.empty();
    }

    return Optional.of(new InternalMatrixRoomState(id));
  }

  @Override
  public MatrixRoomId getId() {
    return id;
  }

  @Override
  public Optional<MatrixRoomAlias> getCanonicalAlias() {
    return Optional.ofNullable(canonicalAlias);
  }

  public void setCanonicalAlias(MatrixRoomAlias canonicalAlias) {
    this.canonicalAlias = canonicalAlias;
  }

  @Override
  public Optional<String> getName() {
    return Optional.ofNullable(name);
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public List<MatrixUser> getRoomUsers() {
    return Collections.unmodifiableList(roomUsers);
  }

  public List<InternalMatrixUserState> getRoomUsersInternal() {
    return roomUsers;
  }

  @Override
  public Optional<MatrixUser> findUserInRoom(MatrixUserId userId) {

    return roomUsers.stream()
        .filter(internalMatrixUserState -> internalMatrixUserState.getId().equals(userId))
        .map(MatrixUser.class::cast)
        .findAny();
  }
}
