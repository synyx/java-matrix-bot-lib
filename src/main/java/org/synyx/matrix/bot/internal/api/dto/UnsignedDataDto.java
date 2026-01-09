package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public record UnsignedDataDto(
    long age,
    MembershipStateDto membership,
    @JsonProperty("prev_content") JsonNode prevContent,
    @JsonProperty("transaction_id") String transactionId) {}
