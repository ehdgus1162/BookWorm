package com.bookworm.application.dto;

/**
 * 단일 도서 대출 요청 DTO
 */
public record SingleLoanRequest(
        Long bookId,
        Long userId,
        Integer loanDays
) {}