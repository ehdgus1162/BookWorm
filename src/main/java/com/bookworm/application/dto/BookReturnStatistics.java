package com.bookworm.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * 도서 반납 통계 DTO
 */
public record BookReturnStatistics(
        @JsonProperty("totalReturns")
        long totalReturns,

        @JsonProperty("overdueReturns")
        long overdueReturns,

        @JsonProperty("onTimeReturns")
        long onTimeReturns,

        @JsonProperty("overdueRate")
        double overdueRate,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @JsonProperty("reportDate")
        LocalDate reportDate
) {

    public static BookReturnStatistics of(long totalReturns, long overdueReturns, LocalDate reportDate) {
        long onTimeReturns = totalReturns - overdueReturns;
        double overdueRate = totalReturns > 0 ? (double) overdueReturns / totalReturns * 100 : 0.0;

        return new BookReturnStatistics(
                totalReturns, overdueReturns, onTimeReturns, overdueRate, reportDate
        );
    }
}