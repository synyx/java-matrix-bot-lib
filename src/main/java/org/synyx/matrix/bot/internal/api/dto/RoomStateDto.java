package org.synyx.matrix.bot.internal.api.dto;

import java.util.List;

public record RoomStateDto(
        List<ClientEventDto> events
) {

}
