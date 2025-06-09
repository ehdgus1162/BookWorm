package com.bookworm.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * 도서 반납 응답 DTO
 */
public record BookReturnResponse(
        @JsonProperty("loanId")
        Long loanId,

        @JsonProperty("bookId")
        Long bookId,

        @JsonProperty("bookTitle")
        String bookTitle,

        @JsonProperty("userId")
        Long userId,

        @JsonProperty("userName")
        String userName,

        @JsonProperty("quantity")
        Integer quantity,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @JsonProperty("loanDate")
        LocalDate loanDate,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @JsonProperty("dueDate")
        LocalDate dueDate,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @JsonProperty("returnDate")
        LocalDate returnDate,

        @JsonProperty("wasOverdue")
        boolean wasOverdue,

        @JsonProperty("overdueDays")
        long overdueDays,

        @JsonProperty("message")
        String message,

        @JsonProperty("success")
        boolean success
) {

    /**
     * 성공적인 반납 응답 생성
     */
    public static BookReturnResponse success(Long loanId, Long bookId, String bookTitle,
                                             Long userId, String userName, Integer quantity,
                                             LocalDate loanDate, LocalDate dueDate, LocalDate returnDate,
                                             boolean wasOverdue, long overdueDays) {
        String message = wasOverdue ?
                String.format("도서가 반납되었습니다. (연체 %d일)", overdueDays) :
                "도서가 정상적으로 반납되었습니다.";

        return new BookReturnResponse(
                loanId, bookId, bookTitle, userId, userName, quantity,
                loanDate, dueDate, returnDate, wasOverdue, overdueDays,
                message, true
        );
    }

    /**
     * 실패한 반납 응답 생성
     */
    public static BookReturnResponse failure(Long loanId, String errorMessage) {
        return new BookReturnResponse(
                loanId, null, null, null, null, null,
                null, null, null, false, 0,
                errorMessage, false
        );
    }
}
