package org.synyx.matrix.bot.internal.api.dto;

public record JoinedRoomDto(
        RoomStateDto state,
        RoomTimelineDto timeline,
        RoomEphemeralDto ephemeral
) {

}
