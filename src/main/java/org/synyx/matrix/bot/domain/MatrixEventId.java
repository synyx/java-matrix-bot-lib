package org.synyx.matrix.bot.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class MatrixEventId {

  /*
  https://spec.matrix.org/v1.14/appendices/#event-ids
   */
  private static final Pattern EVENT_ID_PATTERN = Pattern.compile("^\\$(.+)$");

  private final String opaqueId;

  private MatrixEventId(String opaqueId) {

    this.opaqueId = opaqueId;
  }

  public static Optional<MatrixEventId> from(String value) {

    final var matcher = EVENT_ID_PATTERN.matcher(value);
    if (!matcher.matches()) {
      return Optional.empty();
    }

    final var opaqueId = matcher.group(1);

    return Optional.of(new MatrixEventId(opaqueId));
  }

  public String getOpaqueId() {

    return opaqueId;
  }

  public String getFormatted() {

    return "$%s".formatted(opaqueId);
  }

  @Override
  public boolean equals(Object o) {

    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MatrixEventId that = (MatrixEventId) o;
    return Objects.equals(opaqueId, that.opaqueId);
  }

  @Override
  public int hashCode() {

    return Objects.hashCode(opaqueId);
  }

  @Override
  public String toString() {

    return getFormatted();
  }
}
