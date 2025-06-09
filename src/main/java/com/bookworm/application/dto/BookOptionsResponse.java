package com.bookworm.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BookOptionsResponse(
        @JsonProperty("languages")
        java.util.List<String> languages,

        @JsonProperty("types")
        java.util.List<String> types,

        @JsonProperty("statuses")
        java.util.List<BookStatusOption> statuses
) {
    public static BookOptionsResponse create(java.util.List<String> languages, java.util.List<String> types) {
        java.util.List<BookStatusOption> statuses = java.util.Arrays.stream(com.bookworm.domain.constant.BookStatus.values())
                .map(status -> new BookStatusOption(status.name(), status.getDescription()))
                .toList();

        return new BookOptionsResponse(languages, types, statuses);
    }
}