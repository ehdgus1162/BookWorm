package com.bookworm.application.dto;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * 대출 감사 리포트 DTO
 * - 특정 기간 내 대출 활동 요약
 * - 연체율 및 통계 정보 포함
 */
public record LoanAuditReportDto(
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate reportPeriodStart,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate reportPeriodEnd,

        long totalLoans,
        long overdueLoans,
        long returnedLoans,
        double overdueRate
) {

    /**
     * 리포트 유효성 검증
     */
    public boolean isValid() {
        return reportPeriodStart != null
                && reportPeriodEnd != null
                && !reportPeriodStart.isAfter(reportPeriodEnd)
                && totalLoans >= 0
                && overdueLoans >= 0
                && returnedLoans >= 0
                && overdueRate >= 0.0 && overdueRate <= 100.0;
    }

    /**
     * 간단한 요약 문자열
     */
    public String getSummary() {
        return String.format(
                "기간: %s ~ %s, 총 대출: %d건, 연체: %d건 (%.1f%%)",
                reportPeriodStart, reportPeriodEnd, totalLoans, overdueLoans, overdueRate
        );
    }
}