package com.bookworm.interfaces.common;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 인증 검증 공통 모듈
 *
 * 원리 설명:
 * 1. 책임 분리: 인증 검증 로직만 담당
 * 2. 일관성: 모든 컨트롤러에서 동일한 방식으로 인증 확인
 * 3. 예외 처리: 인증 실패 시 일관된 예외 발생
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationValidator {

    private final SessionManager sessionManager;

    /**
     * 사용자 인증 상태 검증
     * @param request HTTP 요청
     * @return 인증된 사용자 ID
     * @throws UnauthorizedException 인증되지 않은 경우
     */
    public Long validateUserAuthentication(HttpServletRequest request) {
        Long userId = sessionManager.getUserIdFromSession(request);

        if (userId == null) {
            log.warn("인증되지 않은 사용자 접근 시도: IP={}", getClientIp(request));
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        log.debug("사용자 인증 확인 완료: 사용자 ID={}", userId);
        return userId;
    }

    /**
     * 관리자 인증 상태 검증
     * @param request HTTP 요청
     * @return 인증된 관리자 ID
     * @throws UnauthorizedException 인증되지 않은 경우
     */
    public Long validateAdminAuthentication(HttpServletRequest request) {
        Long adminId = sessionManager.getAdminIdFromSession(request);

        if (adminId == null) {
            log.warn("인증되지 않은 관리자 접근 시도: IP={}", getClientIp(request));
            throw new UnauthorizedException("관리자 로그인이 필요합니다.");
        }

        log.debug("관리자 인증 확인 완료: 관리자 ID={}", adminId);
        return adminId;
    }

    /**
     * 사용자 인증 상태 확인 (예외 발생하지 않음)
     * @param request HTTP 요청
     * @return 인증 여부
     */
    public boolean isUserAuthenticated(HttpServletRequest request) {
        return sessionManager.getUserIdFromSession(request) != null;
    }

    /**
     * 관리자 인증 상태 확인 (예외 발생하지 않음)
     * @param request HTTP 요청
     * @return 인증 여부
     */
    public boolean isAdminAuthenticated(HttpServletRequest request) {
        return sessionManager.getAdminIdFromSession(request) != null;
    }

    /**
     * 클라이언트 IP 주소 추출
     * 로깅 및 보안 목적으로 사용
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}

