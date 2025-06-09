package com.bookworm.application.dto;

import java.util.List;

/**
 * 대출 페이지 응답 DTO
 */
public record LoanPageResponse(
        List<LoanResponse> loans,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {}