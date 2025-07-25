package org.synyx.matrix.bot.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatrixRoom {

    /**
     * The id of the room that uniquely identifies it.
     */
    @Getter
    private final MatrixRoomId id;

    /**
     * The alias of the room that is considered the canonical one.
     * This could be for display purposes or as suggestion to users which alias to use to advertise and access the room.
     */
    @Setter
    private MatrixRoomAlias canonicalAlias;

    /**
     * A human-readable name for the room, designated to be displayed to the end-user.
     * The room name is not unique, as multiple rooms can have the same room name set.
     */
    @Setter
    private String name;

    @Getter
    private final List<MatrixUser> roomUsers;

    private MatrixRoom(MatrixRoomId id) {

        this.id = id;
        this.roomUsers = new ArrayList<>();
    }

    public static Optional<MatrixRoom> from(MatrixRoomId id) {

        if (id == null) {
            return Optional.empty();
        }

        return Optional.of(new MatrixRoom(id));
    }

    public Optional<MatrixRoomAlias> getCanonicalAlias() {

        return Optional.ofNullable(canonicalAlias);
    }

    public Optional<String> getName() {

        return Optional.ofNullable(name);
    }

    public Optional<MatrixUser> findUserInRoomById(MatrixUserId userId) {

        return roomUsers.stream()
                .filter(matrixUser -> matrixUser.getId().equals(userId))
                .findAny();
    }
}
