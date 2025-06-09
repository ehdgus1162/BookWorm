package com.bookworm.interfaces.exception;

// 이메일 형식이 유효하지 않은 경우
public class InvalidEmailException extends InvalidInputException {
    public InvalidEmailException(String message) {
        super(message);
    }
}
