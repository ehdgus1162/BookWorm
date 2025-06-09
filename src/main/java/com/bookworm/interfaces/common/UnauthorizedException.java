package com.bookworm.interfaces.common;

/**
 * 인증 예외 클래스
 * 인증 관련 오류를 나타내는 커스텀 예외
 */
class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
