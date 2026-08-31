package org.synyx.matrix.bot.domain.state;

import java.util.List;
import java.util.Optional;
import org.synyx.matrix.bot.domain.MatrixRoomAlias;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixUserId;

public interface MatrixState {

  /** The ID of the account that is logged into the matrix server. */
  MatrixUserId getOwnUserId();

  /** A list of all rooms that the self account is invited to. */
  List<MatrixRoom> getInvitedRooms();

  /** Tries to find a room that the self account is invited to join by its id. */
  Optional<MatrixRoom> findInvitedRoom(MatrixRoomId roomId);

  /** A list of all rooms that the self account has joined. */
  List<MatrixRoom> getJoinedRooms();

  /** Tries to find a room that the self account is joined by its id. */
  Optional<MatrixRoom> findJoinedRoom(MatrixRoomId roomId);

  /** Tries to find a room that the self account is joined by its canonical alias. */
  Optional<MatrixRoom> findJoinedRoom(MatrixRoomAlias alias);
}
