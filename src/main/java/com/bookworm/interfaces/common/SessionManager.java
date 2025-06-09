package com.bookworm.interfaces.common;

import com.bookworm.application.dto.AdminLoginResponse;
import com.bookworm.application.dto.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SessionManager {

    // 세션 타임아웃 상수화 (매직 넘버 제거)
    private static final int USER_SESSION_TIMEOUT = 1800;    // 30분
    private static final int ADMIN_SESSION_TIMEOUT = 3600;   // 1시간

    // 세션 속성명 상수화 (오타 방지 및 일관성)
    public static final String USER_ID_ATTR = "userId";
    public static final String USER_EMAIL_ATTR = "userEmail";
    public static final String USER_FULL_NAME_ATTR = "userFullName";
    public static final String USER_STATUS_ATTR = "userStatus";

    public static final String ADMIN_ID_ATTR = "adminId";
    public static final String ADMIN_EMAIL_ATTR = "adminEmail";
    public static final String ADMIN_FULL_NAME_ATTR = "adminFullName";
    public static final String ADMIN_ROLE_ATTR = "adminRole";
    public static final String ADMIN_STATUS_ATTR = "adminStatus";

    /**
     * 사용자 세션 생성
     * 기존 세션 무효화 → 새 세션 생성 → 사용자 정보 저장
     */
    public void createUserSession(HttpServletRequest request, LoginResponse user) {
        // 1. 기존 세션 무효화 (세션 고정 공격 방지)
        invalidateExistingSession(request);

        // 2. 새 세션 생성 및 사용자 정보 저장
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_ID_ATTR, user.id());
        session.setAttribute(USER_EMAIL_ATTR, user.email());
        session.setAttribute(USER_FULL_NAME_ATTR, user.fullName());
        session.setAttribute(USER_STATUS_ATTR, user.status());
        session.setMaxInactiveInterval(USER_SESSION_TIMEOUT);

        log.info("사용자 세션 생성 완료: 사용자 ID={}, 세션 ID={}", user.id(), session.getId());
    }

    /**
     * 관리자 세션 생성
     * 사용자 세션과 동일한 패턴이지만 다른 속성명과 타임아웃 사용
     */
    public void createAdminSession(HttpServletRequest request, AdminLoginResponse admin) {
        invalidateExistingSession(request);

        HttpSession session = request.getSession(true);
        session.setAttribute(ADMIN_ID_ATTR, admin.id());
        session.setAttribute(ADMIN_EMAIL_ATTR, admin.email());
        session.setAttribute(ADMIN_FULL_NAME_ATTR, admin.fullName());
        session.setAttribute(ADMIN_ROLE_ATTR, admin.role());
        session.setAttribute(ADMIN_STATUS_ATTR, admin.status());
        session.setMaxInactiveInterval(ADMIN_SESSION_TIMEOUT);

        log.info("관리자 세션 생성 완료: 관리자 ID={}, 세션 ID={}", admin.id(), session.getId());
    }

    /**
     * 세션 무효화 (로그아웃)
     * 사용자/관리자 구분 없이 현재 세션을 무효화
     */
    public boolean invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            Long userId = (Long) session.getAttribute(USER_ID_ATTR);
            Long adminId = (Long) session.getAttribute(ADMIN_ID_ATTR);

            log.info("세션 무효화: 사용자 ID={}, 관리자 ID={}, 세션 ID={}",
                    userId, adminId, session.getId());
            session.invalidate();
            return true;
        } else {
            log.warn("무효화할 세션이 없음");
            return false;
        }
    }

    /**
     * 사용자 세션 검증
     * 로그인이 필요한 API에서 사용
     */
    public Long getUserIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(USER_ID_ATTR) == null) {
            return null; // 인증되지 않음
        }

        return (Long) session.getAttribute(USER_ID_ATTR);
    }

    /**
     * 관리자 세션 검증
     * 관리자 권한이 필요한 API에서 사용
     */
    public Long getAdminIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(ADMIN_ID_ATTR) == null) {
            return null; // 인증되지 않음
        }

        return (Long) session.getAttribute(ADMIN_ID_ATTR);
    }

    /**
     * 기존 세션 무효화 (private 헬퍼 메서드)
     * 세션 고정 공격을 방지하기 위해 로그인 시 기존 세션을 제거
     */
    private void invalidateExistingSession(HttpServletRequest request) {
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            log.debug("기존 세션 무효화: 세션 ID={}", existingSession.getId());
            existingSession.invalidate();
        }
    }
}