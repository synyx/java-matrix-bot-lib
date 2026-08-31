package org.synyx.matrix.bot;

import org.synyx.matrix.bot.domain.event.MatrixMessageEvent;
import org.synyx.matrix.bot.domain.event.MatrixRoomInviteEvent;
import org.synyx.matrix.bot.domain.event.MatrixSelfLeaveRoomEvent;
import org.synyx.matrix.bot.domain.event.MatrixUserJoinRoomEvent;
import org.synyx.matrix.bot.domain.event.MatrixUserLeaveRoomEvent;
import org.synyx.matrix.bot.domain.state.MatrixState;
import org.synyx.matrix.bot.internal.MatrixClientImpl;

/**
 * An interface providing callbacks for things happening on the matrix server that were received by
 * the client. All methods have a default implementation that does nothing, so implementing classes
 * only need to override whatever they want to listen to.
 *
 * <p>Any reactions to events happening shall be performed using the appropriate {@link
 * MatrixClientImpl} instance.
 */
public interface MatrixEventConsumer {

  /**
   * The client successfully connected to the server.
   *
   * @param state The state after the initial synchronization.
   */
  default void onConnected(MatrixState state) {}

  /**
   * A message event was received in a room that the client is part of.
   *
   * @param state The current client state.
   * @param event Details about the event that happened.
   */
  default void onMessage(MatrixState state, MatrixMessageEvent event) {}

  /**
   * An invitation to a room was received.
   *
   * @param state The current client state.
   * @param event Details about the event that happened.
   */
  default void onInviteToRoom(MatrixState state, MatrixRoomInviteEvent event) {}

  /**
   * A user joined a room that the client is part of.
   *
   * @param state The current client state.
   * @param event Details about the event that happened.
   */
  default void onUserJoinRoom(MatrixState state, MatrixUserJoinRoomEvent event) {}

  /**
   * A user left a room that the client is part of.
   *
   * @param state The current client state.
   * @param event Details about the event that happened.
   */
  default void onUserLeaveRoom(MatrixState state, MatrixUserLeaveRoomEvent event) {}

  /**
   * The client left a room it was part of. May have been caused by external factors like kicks or
   * bans.
   *
   * @param state The current client state.
   * @param event Details about the event that happened.
   */
  default void onSelfLeaveRoom(MatrixState state, MatrixSelfLeaveRoomEvent event) {}
}
