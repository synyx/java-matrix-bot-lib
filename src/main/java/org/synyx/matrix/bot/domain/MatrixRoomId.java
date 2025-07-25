package org.synyx.matrix.bot.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;
import java.util.regex.Pattern;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatrixRoomId {

    /*
    https://spec.matrix.org/v1.14/appendices/#room-ids
     */
    private static final Pattern ROOM_ID_PATTERN = Pattern.compile("^!([^:\\x00]+):(.+)$");

    @Getter
    private final String opaqueId;
    @Getter
    private final String domain;

    public static Optional<MatrixRoomId> from(String value) {

        final var matcher = ROOM_ID_PATTERN.matcher(value);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        final var opaqueId = matcher.group(1);
        final var domain = matcher.group(2);

        return Optional.of(new MatrixRoomId(opaqueId, domain));
    }

    public String getFormatted() {

        return "!%s:%s".formatted(opaqueId, domain);
    }

    @Override
    public String toString() {

        return getFormatted();
    }
}
