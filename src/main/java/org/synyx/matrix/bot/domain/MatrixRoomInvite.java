package org.synyx.matrix.bot.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatrixRoomInvite {

    @Getter
    private final MatrixRoom room;

    private final MatrixUser invitedBy;

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

    public Optional<MatrixUser> getInvitedBy() {

        return Optional.ofNullable(invitedBy);
    }
}
