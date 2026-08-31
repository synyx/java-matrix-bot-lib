package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UploadMediaResponseDto(@JsonProperty("content_uri") String contentUri) {}
