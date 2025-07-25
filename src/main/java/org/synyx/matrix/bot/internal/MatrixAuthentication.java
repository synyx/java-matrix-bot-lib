package org.synyx.matrix.bot.internal;

import lombok.Getter;
import lombok.Setter;
import org.synyx.matrix.bot.domain.MatrixUserId;

import java.util.Optional;

public class MatrixAuthentication {

    @Getter
    private final String username;
    @Getter
    private final String password;

    @Setter
    private MatrixUserId userId;
    @Setter
    private String bearerToken;

    public MatrixAuthentication(String username, String password) {

        this.username = username;
        this.password = password;
        this.bearerToken = null;
    }

    public boolean isAuthenticated() {

        return bearerToken != null;
    }

    public Optional<String> getBearerToken() {

        return Optional.ofNullable(bearerToken);
    }

    public Optional<MatrixUserId> getUserId() {

        return Optional.ofNullable(userId);
    }
}
