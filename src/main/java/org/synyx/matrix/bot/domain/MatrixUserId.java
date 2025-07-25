package org.synyx.matrix.bot.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;
import java.util.regex.Pattern;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatrixUserId {

    /*
    While newer user IDs follow a stricter pattern, historical IDs are more lenient:
    https://spec.matrix.org/v1.14/appendices/#user-identifiers
     */
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^@([^:\\x00]+):(.+)$");

    @Getter
    private final String localPart;
    @Getter
    private final String domain;

    public static Optional<MatrixUserId> from(String value) {

        final var matcher = USER_ID_PATTERN.matcher(value);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        final var localPart = matcher.group(1);
        final var domain = matcher.group(2);

        return Optional.of(new MatrixUserId(localPart, domain));
    }

    public String getFormatted() {

        return "@%s:%s".formatted(localPart, domain);
    }

    @Override
    public String toString() {

        return getFormatted();
    }
}
