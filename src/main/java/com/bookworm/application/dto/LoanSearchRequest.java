package com.bookworm.application.dto;


import java.time.LocalDate;

/**
 * 대출 검색 요청 DTO
 */
public record LoanSearchRequest(
        String userName,
        String bookTitle,
        String status,
        LocalDate loanDateFrom,
        LocalDate loanDateTo,
        LocalDate dueDateFrom,
        LocalDate dueDateTo,
        Boolean overdue,
        Integer page,
        Integer size
) {}