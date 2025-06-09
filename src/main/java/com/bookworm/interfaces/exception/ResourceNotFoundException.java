package com.bookworm.interfaces.exception;

/**
 * 요청한 리소스를 찾을 수 없을 때 발생하는 예외
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 기본 생성자
     */
    public ResourceNotFoundException() {
        super("요청한 리소스를 찾을 수 없습니다.");
    }

    /**
     * 메시지를 지정하는 생성자
     *
     * @param message 예외 메시지
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인을 지정하는 생성자
     *
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}