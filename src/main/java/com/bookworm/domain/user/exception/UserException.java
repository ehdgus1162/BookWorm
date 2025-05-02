package com.bookworm.domain.user.exception;

import com.bookworm.common.error.DomainException;

public class UserException extends DomainException {
    public UserException(String message) {
        super(message);
    }
}
