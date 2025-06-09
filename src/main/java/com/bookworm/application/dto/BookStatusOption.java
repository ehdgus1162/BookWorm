package com.bookworm.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 도서 상태 옵션 DTO
 */
public record BookStatusOption(
        @JsonProperty("value")
        String value,

        @JsonProperty("label")
        String label
) {}