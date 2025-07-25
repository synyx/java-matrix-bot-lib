package org.synyx.matrix.bot.internal.api.dto.event;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MemberEventContentDto(
        @JsonProperty("displayname")
        String displayName,
        MembershipState membership
) {

    public static final String TYPE = "m.room.member";

    public enum MembershipState {
        @JsonEnumDefaultValue
        UNKNOWN,

        @JsonProperty("invite")
        INVITE,
        @JsonProperty("join")
        JOIN,
        @JsonProperty("leave")
        LEAVE,
        @JsonProperty("ban")
        BAN,
        @JsonProperty("knock")
        KNOCK
    }
}
