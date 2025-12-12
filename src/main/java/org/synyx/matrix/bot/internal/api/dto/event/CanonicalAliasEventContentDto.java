package org.synyx.matrix.bot.internal.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CanonicalAliasEventContentDto(
    String alias, @JsonProperty("alt_aliases") List<String> altAliases) {

  public static final String TYPE = "m.room.canonical_alias";
}
