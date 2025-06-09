package com.bookworm.domain.exception;

/**
 * 대출 도메인 비즈니스 예외
 * - 대출 관련 비즈니스 규칙 위반 시 발생
 * - 명확한 오류 메시지 제공
 */
public class LoanBusinessException extends RuntimeException {

    public LoanBusinessException(String message) {
        super(message);
    }

    public LoanBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

