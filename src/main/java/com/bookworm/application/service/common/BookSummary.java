package com.bookworm.application.service.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 도서 요약 통계
 */
public record BookSummary(
        @JsonProperty("totalBooks")
        Long totalBooks,

        @JsonProperty("availableBooks")
        Long availableBooks,

        @JsonProperty("borrowedBooks")
        Long borrowedBooks
) {}