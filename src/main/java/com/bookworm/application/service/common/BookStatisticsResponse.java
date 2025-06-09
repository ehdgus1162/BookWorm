package com.bookworm.application.service.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * 도서 통계 응답 DTO
 */
public record BookStatisticsResponse(
        @JsonProperty("statusCounts")
        Map<String, Long> statusCounts,

        @JsonProperty("typeCounts")
        Map<String, Long> typeCounts,

        @JsonProperty("languageCounts")
        Map<String, Long> languageCounts,

        @JsonProperty("inventory")
        InventoryStatistics inventory,

        @JsonProperty("summary")
        BookSummary summary
) {
    public static BookStatisticsResponse from(BookStatisticsData data) {
        InventoryStatistics inventory = new InventoryStatistics(
                data.totalQuantity(),
                data.availableQuantity(),
                data.borrowedQuantity()
        );

        BookSummary summary = new BookSummary(
                data.statusCounts().values().stream().mapToLong(Long::longValue).sum(),
                data.statusCounts().getOrDefault("AVAILABLE", 0L),
                data.statusCounts().getOrDefault("BORROWED", 0L)
        );

        return new BookStatisticsResponse(
                data.statusCounts(),
                data.typeCounts(),
                data.languageCounts(),
                inventory,
                summary
        );
    }
}
