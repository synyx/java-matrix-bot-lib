package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SyncResponseDto(
        @JsonProperty("next_batch")
        String nextBatch,
        SyncRoomsDto rooms
) {

}
