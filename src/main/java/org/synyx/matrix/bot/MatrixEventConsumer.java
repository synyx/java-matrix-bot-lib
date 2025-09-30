package org.synyx.matrix.bot;

import org.synyx.matrix.bot.domain.MatrixMessage;
import org.synyx.matrix.bot.domain.MatrixRoom;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixRoomInvite;
import org.synyx.matrix.bot.domain.MatrixUserId;

/**
 * An interface providing callbacks for things happening on the matrix server that were received by the client.
 * All methods have a default implementation that does nothing, so implementing classes only need to override whatever
 * they want to listen to.
 * <p>
 * Any reactions to events happening shall be performed using the appropriate {@link MatrixClient} instance.
 */
public interface MatrixEventConsumer {

  /**
   * The client successfully connected to the server.
   *
   * @param state The state after the initial synchronisation.
   */
  default void onConnected(MatrixState state) {

  }

  /**
   * A message event was received in a room that the client is part of.
   *
   * @param state   The current client state.
   * @param room    The room the message was received in.
   * @param message The message that was received.
   */
  default void onMessage(MatrixState state, MatrixRoom room, MatrixMessage message) {

  }

  /**
   * An invitation to a room was received.
   *
   * @param state  The current client state.
   * @param invite The invite that was received.
   */
  default void onInviteToRoom(MatrixState state, MatrixRoomInvite invite) {

  }

  /**
   * A user joined a room that the client is part of.
   *
   * @param state  The current client state.
   * @param room   The room that the user joined in.
   * @param userId The id of the user that joined the room.
   */
  default void onUserJoinRoom(MatrixState state, MatrixRoom room, MatrixUserId userId) {

  }

  /**
   * A user left a room that the client is part of.
   *
   * @param state  The current client state.
   * @param room   The room that the user left from.
   * @param userId The id of the user that left the room.
   */
  default void onUserLeaveRoom(MatrixState state, MatrixRoom room, MatrixUserId userId) {

  }

  /**
   * The client left a room it was part of. May have been caused by external factors like kicks or bans.
   *
   * @param state  The current client state.
   * @param roomId The id of the room that the client left from.
   */
  default void onSelfLeaveRoom(MatrixState state, MatrixRoomId roomId) {

  }
}
