package org.synyx.matrix.bot.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class MatrixRoomAlias {

  /*
  https://spec.matrix.org/v1.14/appendices/#room-aliases
   */
  private static final Pattern ROOM_ALIAS_PATTERN = Pattern.compile("^#([^:\\x00]+):(.+)$");

  private final String localPart;
  private final String domain;

  private MatrixRoomAlias(String localPart, String domain) {

    this.localPart = localPart;
    this.domain = domain;
  }

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

  public String getLocalPart() {

    return localPart;
  }

  public String getDomain() {

    return domain;
  }

  public String getFormatted() {

    return "#%s:%s".formatted(localPart, domain);
  }

  @Override
  public boolean equals(Object o) {

    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MatrixRoomAlias that = (MatrixRoomAlias) o;
    return Objects.equals(localPart, that.localPart) && Objects.equals(domain, that.domain);
  }

  @Override
  public int hashCode() {

    return Objects.hash(localPart, domain);
  }

  @Override
  public String toString() {

    return getFormatted();
  }
}
