package com.bookworm.interfaces.exception;

/**
 * 비즈니스 예외의 기본 클래스
 *
 * 모든 비즈니스 로직 관련 예외의 부모 클래스
 * RuntimeException을 상속하여 Unchecked Exception으로 처리
 */
public abstract class BusinessException extends RuntimeException {

    protected BusinessException(String message) {
        super(message);
    }

    protected BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    protected BusinessException(Throwable cause) {
        super(cause);
    }

    /**
     * 예외의 비즈니스 카테고리 반환
     * 하위 클래스에서 오버라이드하여 카테고리 지정
     */
    public String getCategory() {
        return "BUSINESS";
    }

    /**
     * 예외의 심각도 레벨 반환
     */
    public ErrorLevel getErrorLevel() {
        return ErrorLevel.WARN;
    }

    /**
     * 에러 심각도 열거형
     */
    public enum ErrorLevel {
        INFO, WARN, ERROR, FATAL
    }
}