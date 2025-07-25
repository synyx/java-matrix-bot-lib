package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public record StrippedStateEventDto(
        JsonNode content,
        String sender,
        @JsonProperty("state_key")
        String stateKey,
        String type
) {

}
