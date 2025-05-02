package com.bookworm.domain.user.exception;

public class UserInvalidPasswordException extends RuntimeException {
    public UserInvalidPasswordException(String message) {
        super(message);
    }
}
