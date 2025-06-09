package com.bookworm.application.service.common;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 재고 통계
 */
public record InventoryStatistics(
        @JsonProperty("totalQuantity")
        Long totalQuantity,

        @JsonProperty("availableQuantity")
        Long availableQuantity,

        @JsonProperty("borrowedQuantity")
        Long borrowedQuantity
) {}