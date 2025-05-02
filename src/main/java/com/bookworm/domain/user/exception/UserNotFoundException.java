package com.bookworm.domain.user.exception;

public class UserNotFoundException extends UserException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
