package org.synyx.matrix.bot;

import org.synyx.matrix.bot.domain.MatrixEventId;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.internal.MatrixClientImpl;

import java.util.Optional;

/**
 * An interface for a client connecting to a matrix server.
 * Serves as the main method of communicating with the server.
 */
public interface MatrixClient {

  /**
   * Creates a new matrix client to connect to the specified server.
   *
   * @param url      The url for connecting to the intended matrix server. Must start with http:// or https://
   * @param username The username for logging into the matrix server.
   * @param password The password for logging into the matrix server.
   * @return A {@link MatrixClient} implementation that connects to the specified matrix server.
   */
  static MatrixClient create(String url, String username, String password) {

    return new MatrixClientImpl(url, username, password);
  }

  /**
   * Sets a consumer object that gets called on events happening on the matrix server.
   * Only one consumer can be set at any time.
   * Calling this method again replaces any previous event callback.
   *
   * @param eventConsumer The consumer to call on events.
   */
  void setEventCallback(MatrixEventConsumer eventConsumer);

  /**
   * Optionally provides an interface to provide the current state of the matrix client.
   * If not provided, any startup will act like the first startup and will ignore any previously sent messages.
   * Providing a persisted state will make the client be able to determine which events happened while offline.
   *
   * @param persistedState An interface for persisting the matrix client state
   */
  void setPersistedStateProvider(MatrixPersistedStateProvider persistedState);


  /**
   * The main matrix client event loop that continuously syncs all events happening on the matrix server to the client.
   * This is a blocking call, so make sure to call it from a different thread if needed.
   *
   * @throws InterruptedException The sync has been interrupted
   */
  void syncContinuous() throws InterruptedException;

  /**
   * Requests the matrix client to stop syncing and terminate.
   * May be called from a different thread.
   */
  void requestStopOfSync();


  /**
   * Returns whether the matrix client is currently connected to the server or not.
   *
   * @return {@code true} if the client is currently connected to the server, {@code false} otherwise.
   */
  boolean isConnected();

  /**
   * Returns the current state of the matrix client.
   *
   * @return A {@link MatrixState} object if currently connected to a server, {@link Optional#empty()} otherwise.
   */
  Optional<MatrixState> getState();

  /**
   * Attempts to send a message to the specified room.
   *
   * @param roomId      The id of the room to send the message to.
   * @param messageBody The body of the message to send.
   * @return A {@link MatrixEventId} containing the id of the event that was sent or {@link Optional#empty()} if sending the message did not succeed.
   */
  Optional<MatrixEventId> sendMessage(MatrixRoomId roomId, String messageBody);

  /**
   * Attempts to add a reaction to an event (a message of the time).
   *
   * @param roomId   The id of the room to send the message in.
   * @param eventId  The id of the event to react to.
   * @param reaction The reaction to send.
   * @return A {@link MatrixEventId} containing the id of the event that was sent or {@link Optional#empty()} if sending the reaction did not succeed.
   */
  Optional<MatrixEventId> addReaction(MatrixRoomId roomId, MatrixEventId eventId, String reaction);

  /**
   * Attempts to join a room.
   *
   * @param roomId The id of the room to join.
   * @return {@code true} if joining the room was successful, {@code false} otherwise.
   */
  boolean joinRoom(MatrixRoomId roomId);

  /**
   * Attempts to leave a room.
   *
   * @param roomId The id of the room to leave.
   * @return {@code true} if leaving the room was successful, {@code false} otherwise.
   */
  boolean leaveRoom(MatrixRoomId roomId);
}
