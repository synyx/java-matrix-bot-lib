package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public record ClientEventDto(
        JsonNode content,
        @JsonProperty("event_id")
        String eventId,
        @JsonProperty("origin_server_ts")
        long originServerTs,
        String sender,
        @JsonProperty("state_key")
        String stateKey,
        String type
) {

}
