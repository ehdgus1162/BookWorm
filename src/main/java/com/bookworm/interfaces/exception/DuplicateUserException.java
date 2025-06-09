package com.bookworm.interfaces.exception;

// 중복된 사용자가 있는 경우
public class DuplicateUserException extends BookwormException {
    public DuplicateUserException(String message) {
        super(message);
    }
}
