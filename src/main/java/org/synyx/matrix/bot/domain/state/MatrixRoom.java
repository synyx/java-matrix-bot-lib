package org.synyx.matrix.bot.domain.state;

import java.util.List;
import java.util.Optional;
import org.synyx.matrix.bot.domain.MatrixRoomAlias;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixUserId;

public interface MatrixRoom {

  /** The id of the room that uniquely identifies it. */
  MatrixRoomId getId();

  /**
   * The alias of the room that is considered the canonical one. This could be for display purposes
   * or as suggestion to users which alias to use to advertise and access the room.
   */
  Optional<MatrixRoomAlias> getCanonicalAlias();

  /**
   * A human-readable name for the room, designated to be displayed to the end-user. The room name
   * is not unique, as multiple rooms can have the same room name set.
   */
  Optional<String> getName();

  /** A list of all users that are part of the room. */
  List<MatrixUser> getRoomUsers();

  /** Tries to find a user in the room by its id. */
  Optional<MatrixUser> findUserInRoom(MatrixUserId userId);
}
