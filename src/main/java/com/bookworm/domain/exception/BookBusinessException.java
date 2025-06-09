package com.bookworm.domain.exception;


/**
 * 도서 관련 비즈니스 예외
 */
public class BookBusinessException extends RuntimeException {

    public BookBusinessException(String message) {
        super(message);
    }

    public BookBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}