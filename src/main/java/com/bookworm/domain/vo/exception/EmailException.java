package com.bookworm.domain.vo.exception;

import com.bookworm.common.error.DomainException;

public class EmailException extends DomainException {
    public EmailException(String message) {
        super(message);
    }

    public static class EmptyEmailException extends EmailException {
      public EmptyEmailException(String message) {
        super(message);
      }
    }

    public static class InvalidEmailFormatException extends EmailException {
      public InvalidEmailFormatException(String message) {
        super(message);
      }
    }
}
