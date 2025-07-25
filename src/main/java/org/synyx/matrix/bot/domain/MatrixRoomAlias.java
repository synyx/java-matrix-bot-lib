package org.synyx.matrix.bot.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;
import java.util.regex.Pattern;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatrixRoomAlias {

    /*
    https://spec.matrix.org/v1.14/appendices/#room-aliases
     */
    private static final Pattern ROOM_ALIAS_PATTERN = Pattern.compile("^#([^:\\x00]+):(.+)$");

    @Getter
    private final String localPart;
    @Getter
    private final String domain;

    public static Optional<MatrixRoomAlias> from(String value) {

        final var matcher = ROOM_ALIAS_PATTERN.matcher(value);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        final var localPart = matcher.group(1);
        final var domain = matcher.group(2);

        return Optional.of(new MatrixRoomAlias(localPart, domain));
    }

    public static Optional<MatrixRoomAlias> build(String localPart, String domain) {

        return from("#%s:%s".formatted(localPart, domain));
    }

    public String getFormatted() {

        return "#%s:%s".formatted(localPart, domain);
    }

    @Override
    public String toString() {

        return getFormatted();
    }
}
