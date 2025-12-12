package org.synyx.matrix.bot.internal.api.dto.event;

public record RoomNameEventContentDto(String name) {

  public static final String TYPE = "m.room.name";
}
