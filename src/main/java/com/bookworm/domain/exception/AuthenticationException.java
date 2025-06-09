package com.bookworm.domain.exception;

/**
 * 인증 실패 예외
 * 로그인 시 이메일/비밀번호 불일치, 계정 비활성화 등의 상황에서 발생
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}