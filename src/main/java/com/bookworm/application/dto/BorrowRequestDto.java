package com.bookworm.application.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 대출 요청 DTO
 */
public record BorrowRequestDto(
        Long userId,
        List<Long> bookIds,
        LocalDate dueDate
) {

    /**
     * 선택된 도서 ID 목록으로 대출 요청 생성
     */
    public static BorrowRequestDto of(Long userId, List<Long> bookIds, LocalDate dueDate) {
        return new BorrowRequestDto(userId, bookIds, dueDate);
    }
}