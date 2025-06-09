package com.bookworm.domain.exception;

public class UserBusinessException extends LibraryBusinessException {
    public UserBusinessException(String message) {
        super("USER_BUSINESS_ERROR", message);

    }
}