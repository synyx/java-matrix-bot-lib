package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MatrixLoginResponseDto(
    @JsonProperty("user_id") String userId, @JsonProperty("access_token") String accessToken) {}
