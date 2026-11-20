package org.synyx.matrix.bot.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MatrixRoomIdTest {

  @Test
  void ensureCanParseRoomIdWithDomainPart() {

    final var result = MatrixRoomId.from("!opaque_id:domain");

    assertThat(result).isPresent();
    assertThat(result.map(MatrixRoomId::getOpaqueId)).hasValue("opaque_id");
    assertThat(result.flatMap(MatrixRoomId::getDomain)).hasValue("domain");
  }

  @Test
  void ensureCanParseRoomIdWithoutDomainPart() {

    final var result = MatrixRoomId.from("!opaque_id");

    assertThat(result).isPresent();
    assertThat(result.map(MatrixRoomId::getOpaqueId)).hasValue("opaque_id");
    assertThat(result.flatMap(MatrixRoomId::getDomain)).isEmpty();
  }

  @Test
  void ensureProperlyFormatsIdWithDomainPart() {

    final var result = MatrixRoomId.from("!opaque_id:domain");

    assertThat(result).isPresent();
    assertThat(result.map(MatrixRoomId::getFormatted)).hasValue("!opaque_id:domain");
  }

  @Test
  void ensureProperlyFormatsIdWithoutDomainPart() {

    final var result = MatrixRoomId.from("!opaque_id");

    assertThat(result).isPresent();
    assertThat(result.map(MatrixRoomId::getFormatted)).hasValue("!opaque_id");
  }
}
