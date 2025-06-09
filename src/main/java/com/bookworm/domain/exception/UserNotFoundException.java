package com.bookworm.domain.exception;

/**
 * 요청한 사용자를 찾을 수 없을 때 발생하는 예외
 *
 * 주로 ID나 이메일로 사용자를 검색했을 때 해당 사용자가 존재하지 않는 경우 발생합니다.
 * 이 예외는 서비스 계층에서 발생하며, 컨트롤러 계층에서 적절한 HTTP 응답(404 Not Found)으로
 * 변환되어야 합니다.
 */

public class UserNotFoundException extends LibraryBusinessException {
    public UserNotFoundException(String identifier) {
        super("USER_NOT_FOUND", "사용자를 찾을 수 없습니다: " + identifier);
    }

    public UserNotFoundException(Long id) {
        super("USER_NOT_FOUND", "사용자를 찾을 수 없습니다. ID: " + id);
    }
}
