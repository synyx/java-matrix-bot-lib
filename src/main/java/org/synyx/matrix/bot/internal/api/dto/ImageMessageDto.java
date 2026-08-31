package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImageMessageDto(
    String body, String msgtype, @JsonProperty("filename") String fileName, String url) {}
