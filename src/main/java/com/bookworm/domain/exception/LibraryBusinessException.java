package com.bookworm.domain.exception;


import lombok.Getter;

@Getter
public class LibraryBusinessException extends RuntimeException {
    private final String errorCode;

    public LibraryBusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public LibraryBusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}