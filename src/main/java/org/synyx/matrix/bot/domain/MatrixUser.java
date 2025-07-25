package org.synyx.matrix.bot.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

public class MatrixUser {

    @Getter
    private final MatrixUserId id;

    @Setter
    private String displayName;

    private MatrixUser(MatrixUserId id) {

        this.id = id;
        this.displayName = null;
    }

    public static Optional<MatrixUser> from(MatrixUserId id) {

        if (id == null) {
            return Optional.empty();
        }

        return Optional.of(new MatrixUser(id));
    }

    public Optional<String> getDisplayName() {

        return Optional.ofNullable(displayName);
    }
}
