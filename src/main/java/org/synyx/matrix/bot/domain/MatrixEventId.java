package org.synyx.matrix.bot.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;
import java.util.regex.Pattern;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatrixEventId {

    /*
    https://spec.matrix.org/v1.14/appendices/#event-ids
     */
    private static final Pattern EVENT_ID_PATTERN = Pattern.compile("^\\$(.+)$");

    @Getter
    private final String opaqueId;

    public static Optional<MatrixEventId> from(String value) {

        final var matcher = EVENT_ID_PATTERN.matcher(value);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        final var opaqueId = matcher.group(1);

        return Optional.of(new MatrixEventId(opaqueId));
    }

    public String getFormatted() {

        return "$%s".formatted(opaqueId);
    }

    @Override
    public String toString() {

        return getFormatted();
    }
}
