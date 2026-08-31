package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImageInfoDto(
    Integer h,
    Integer w,
    Integer size,
    @JsonProperty("is_animated") Boolean isAnimated,
    @JsonProperty("mimetype") String mimeType) {}
