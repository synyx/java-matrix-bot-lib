package org.synyx.matrix.bot;

import lombok.Getter;
import org.synyx.matrix.bot.domain.MatrixRoom;
import org.synyx.matrix.bot.domain.MatrixRoomAlias;
import org.synyx.matrix.bot.domain.MatrixUserId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatrixState {

    @Getter
    private final MatrixUserId ownUserId;

    @Getter
    private final List<MatrixRoom> invitedRooms;

    @Getter
    private final List<MatrixRoom> joinedRooms;

    public MatrixState(MatrixUserId ownUserId) {

        this.ownUserId = ownUserId;
        this.invitedRooms = new ArrayList<>();
        this.joinedRooms = new ArrayList<>();
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
