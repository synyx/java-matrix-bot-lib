package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record InvitedRoomDto(
        @JsonProperty("invite_state")
        InviteStateDto inviteState
) {

    public record InviteStateDto(
            List<StrippedStateEventDto> events
    ) {

    }
}
