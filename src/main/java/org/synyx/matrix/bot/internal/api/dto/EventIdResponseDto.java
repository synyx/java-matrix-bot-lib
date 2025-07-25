package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EventIdResponseDto(
        @JsonProperty("event_id")
        String eventId
) {

}
