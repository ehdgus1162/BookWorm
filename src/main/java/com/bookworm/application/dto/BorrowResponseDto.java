package com.bookworm.application.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 대출 응답 DTO
 */
public record BorrowResponseDto(
        Long loanId,
        Long userId,
        String userName,
        List<BookDto> borrowedBooks,
        LocalDate loanDate,
        LocalDate dueDate,
        Integer totalBooks
) {

    /**
     * 대출 성공 응답 생성
     */
    public static BorrowResponseDto success(Long loanId, Long userId, String userName,
                                            List<BookDto> books, LocalDate loanDate, LocalDate dueDate) {
        return new BorrowResponseDto(
                loanId,
                userId,
                userName,
                books,
                loanDate,
                dueDate,
                books.size()
        );
    }
}