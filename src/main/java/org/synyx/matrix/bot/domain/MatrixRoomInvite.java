package org.synyx.matrix.bot.domain;

import java.util.Optional;

public class MatrixRoomInvite {

    private final MatrixRoom room;
    private final MatrixUser invitedBy;

    private MatrixRoomInvite(MatrixRoom room, MatrixUser invitedBy) {

        this.room = room;
        this.invitedBy = invitedBy;
    }

    public static Optional<MatrixRoomInvite> from(
            MatrixRoom room,
            MatrixUser invitedBy
    ) {

        if (room == null) {
            return Optional.empty();
        }

        return Optional.of(new MatrixRoomInvite(
                room,
                invitedBy
        ));
    }

    public MatrixRoom getRoom() {

        return room;
    }

    public Optional<MatrixUser> getInvitedBy() {

        return Optional.ofNullable(invitedBy);
    }
}
