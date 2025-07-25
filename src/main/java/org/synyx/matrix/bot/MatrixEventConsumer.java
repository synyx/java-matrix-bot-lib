package org.synyx.matrix.bot;

import org.synyx.matrix.bot.domain.MatrixMessage;
import org.synyx.matrix.bot.domain.MatrixRoom;
import org.synyx.matrix.bot.domain.MatrixRoomId;
import org.synyx.matrix.bot.domain.MatrixRoomInvite;
import org.synyx.matrix.bot.domain.MatrixUserId;

public interface MatrixEventConsumer {

    default void onConnected(MatrixState state) {

    }

    default void onMessage(MatrixState state, MatrixRoom room, MatrixMessage message) {

    }

    default void onInviteToRoom(MatrixState state, MatrixRoomInvite invite) {

    }

    default void onUserJoinRoom(MatrixState state, MatrixRoom room, MatrixUserId userId) {

    }

    default void onUserLeaveRoom(MatrixState state, MatrixRoom room, MatrixUserId userId) {

    }

    default void onSelfLeaveRoom(MatrixState state, MatrixRoomId roomId) {

    }
}
