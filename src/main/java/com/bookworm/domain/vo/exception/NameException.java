package com.bookworm.domain.vo.exception;

import com.bookworm.common.error.ErrorMessage;
import com.bookworm.common.error.DomainException;

public class NameException extends DomainException {
    public NameException(String message) {
        super(message);
    }

    /**
     * 첫 번째 이름이 비어있을 때 발생하는 예외
     */
    public static class EmptyNameException extends NameException {
        public EmptyNameException() {
            super(ErrorMessage.Name.EMPTY);
        }
    }

    public static class InvalidNameLengthException extends NameException {
        public InvalidNameLengthException(String message) {
            super(message);
        }
    }
}
