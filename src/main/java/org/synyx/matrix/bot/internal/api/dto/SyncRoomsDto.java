package org.synyx.matrix.bot.internal.api.dto;

import java.util.Map;

public record SyncRoomsDto(
        Map<String, InvitedRoomDto> invite,
        Map<String, JoinedRoomDto> join,
        Map<String, LeftRoomDto> leave
) {

}
