package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RoomTimelineDto(
    List<ClientEventDto> events, Boolean limited, @JsonProperty("prev_batch") String prevBatch) {}
