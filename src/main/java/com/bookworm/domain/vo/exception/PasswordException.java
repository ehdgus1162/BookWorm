package com.bookworm.domain.vo.exception;

import com.bookworm.common.error.ErrorMessage;
import com.bookworm.common.error.DomainException;

import java.util.List;


/**
 * 비밀번호 관련 예외
 */
public class PasswordException extends DomainException {

    public PasswordException(String message) {
        super(message);
    }

    protected PasswordException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 비밀번호가 비어있을 때 발생하는 예외
     */
    public static class EmptyPasswordException extends PasswordException {

        // 단일 정책이기에 메세지 고정시킴
        public EmptyPasswordException() {
            super(ErrorMessage.Password.REQUIRED);
        }
    }

    /**
     * 비밀번호 형식이 유효하지 않을 때 발생하는 예외
     */
    public static class InvalidPasswordFormatException extends PasswordException {

        // 검증 시 리스트 형태로 받을 때
        public InvalidPasswordFormatException(List<String> violations) {
            super(String.join(", ", violations));
        }
    }

    /**
     * 비밀번호 암호화 과정에서 오류가 발생했을 때의 예외
     */
    public static class PasswordEncryptionException extends PasswordException {

        public PasswordEncryptionException(String message) {
            super(message);
        }

        public PasswordEncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 비밀번호가 일치하지 않을 때 발생하는 예외
     */
    public static class PasswordMismatchException extends PasswordException {
        public PasswordMismatchException() {
            super(ErrorMessage.Password.MISMATCH);
        }
    }

}
