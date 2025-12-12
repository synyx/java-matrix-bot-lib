package org.synyx.matrix.bot.internal.api.dto;

public record MatrixLoginDto(MatrixIdentifierDto identifier, String password, String type) {}
