package org.synyx.matrix.bot.internal.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.synyx.matrix.bot.internal.api.dto.MembershipStateDto;

public record MemberEventContentDto(
    @JsonProperty("displayname") String displayName, MembershipStateDto membership) {

  public static final String TYPE = "m.room.member";
}
