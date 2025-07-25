package org.synyx.matrix.bot.internal.api.dto.event;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageEventContentDto(
        String body,
        @JsonProperty("msgtype")
        MessageType messageType
) {

    public static final String TYPE = "m.room.message";

    public enum MessageType {
        @JsonEnumDefaultValue
        UNKNOWN,
        @JsonProperty("m.text")
        TEXT,
        @JsonProperty("m.emote")
        EMOTE,
        @JsonProperty("m.notice")
        NOTICE,
        @JsonProperty("m.image")
        IMAGE,
        @JsonProperty("m.file")
        FILE,
        @JsonProperty("m.audio")
        AUDIO,
        @JsonProperty("m.location")
        LOCATION,
        @JsonProperty("m.video")
        VIDEO
    }
}
