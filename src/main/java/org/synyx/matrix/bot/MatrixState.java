package org.synyx.matrix.bot;

import org.synyx.matrix.bot.domain.MatrixRoom;
import org.synyx.matrix.bot.domain.MatrixRoomAlias;
import org.synyx.matrix.bot.domain.MatrixUserId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatrixState {

    private final MatrixUserId ownUserId;
    private final List<MatrixRoom> invitedRooms;
    private final List<MatrixRoom> joinedRooms;

    public MatrixState(MatrixUserId ownUserId) {

        this.ownUserId = ownUserId;
        this.invitedRooms = new ArrayList<>();
        this.joinedRooms = new ArrayList<>();
    }

    public MatrixUserId getOwnUserId() {
      return ownUserId;
    }

    public List<MatrixRoom> getInvitedRooms() {
      return invitedRooms;
    }

    public List<MatrixRoom> getJoinedRooms() {
      return joinedRooms;
    }

    public Optional<MatrixRoom> findJoinedRoomByCanonicalAlias(MatrixRoomAlias alias) {

        return joinedRooms.stream()
                .filter(room -> room.getCanonicalAlias()
                        .map(matrixRoomAlias -> matrixRoomAlias.equals(alias))
                        .orElse(false)
                )
                .findFirst();
    }
}
