package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record EventDto(
        JsonNode content,
        String type
) {

}
