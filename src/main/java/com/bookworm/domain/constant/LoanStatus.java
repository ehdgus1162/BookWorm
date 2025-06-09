package com.bookworm.domain.constant;

import lombok.Getter;

/**
 * 도서 대출 상태 열거형
 * - 대출의 생명주기를 표현
 */
@Getter
public enum LoanStatus {

    /**
     * 활성 대출 - 현재 대출 중인 상태
     */
    ACTIVE("대출 중"),

    /**
     * 반납 완료 - 정상적으로 반납된 상태
     */
    RETURNED("반납 완료"),

    /**
     * 대출 취소 - 대출이 취소된 상태
     */
    CANCELLED("대출 취소");

    private final String description;

    LoanStatus(String description) {
        this.description = description;
    }

}