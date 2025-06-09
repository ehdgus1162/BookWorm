package com.bookworm.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * 도서 반납 요청 DTO
 */
public record BookReturnRequest(
        @NotNull(message = "대출 ID는 필수입니다.")
        @JsonProperty("loanId")
        Long loanId,

        @JsonProperty("returnNote")
        String returnNote  // 반납 시 특이사항 메모 (선택사항)
) {

    /**
     * 기본 반납 요청 생성
     */
    public static BookReturnRequest of(Long loanId) {
        return new BookReturnRequest(loanId, null);
    }

    /**
     * 메모가 있는 반납 요청 생성
     */
    public static BookReturnRequest withNote(Long loanId, String note) {
        return new BookReturnRequest(loanId, note);
    }
}