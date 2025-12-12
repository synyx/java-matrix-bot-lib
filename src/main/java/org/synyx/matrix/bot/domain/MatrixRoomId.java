package org.synyx.matrix.bot.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class MatrixRoomId {

    /*
    https://spec.matrix.org/v1.14/appendices/#room-ids
     */
    private static final Pattern ROOM_ID_PATTERN = Pattern.compile("^!([^:\\x00]+):(.+)$");

    private final String opaqueId;
    private final String domain;

    private MatrixRoomId(String opaqueId, String domain) {

        this.opaqueId = opaqueId;
        this.domain = domain;
    }

    public static Optional<MatrixRoomId> from(String value) {

        final var matcher = ROOM_ID_PATTERN.matcher(value);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        final var opaqueId = matcher.group(1);
        final var domain = matcher.group(2);

        return Optional.of(new MatrixRoomId(opaqueId, domain));
    }

    public String getOpaqueId() {

        return opaqueId;
    }

    public String getDomain() {

        return domain;
    }

    public String getFormatted() {

        return "!%s:%s".formatted(opaqueId, domain);
    }

    @Override
    public boolean equals(Object o) {

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MatrixRoomId that = (MatrixRoomId) o;
        return Objects.equals(opaqueId, that.opaqueId) && Objects.equals(domain, that.domain);
    }

    @Override
    public int hashCode() {

        return Objects.hash(opaqueId, domain);
    }

    @Override
    public String toString() {

        return getFormatted();
    }
}
