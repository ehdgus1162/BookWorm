package com.bookworm.interfaces.exception;

// 유효하지 않은 입력값이 있는 경우
public class InvalidInputException extends BookwormException {
    public InvalidInputException(String message) {
        super(message);
    }
}
