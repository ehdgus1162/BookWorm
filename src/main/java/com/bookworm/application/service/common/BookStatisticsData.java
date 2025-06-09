package com.bookworm.application.service.common;

import java.util.Map;

/**
 * 통계 데이터 전달용 내부 DTO
 */
public record BookStatisticsData(
        Map<String, Long> statusCounts,
        Map<String, Long> typeCounts,
        Map<String, Long> languageCounts,
        Long totalQuantity,
        Long availableQuantity,
        Long borrowedQuantity
) {}