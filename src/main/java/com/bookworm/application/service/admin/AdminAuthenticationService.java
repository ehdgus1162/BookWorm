package com.bookworm.application.service.admin;


import com.bookworm.application.dto.AdminLoginResponse;

/**
 * 관리자 인증 서비스 인터페이스
 * 사용자용 UserAuthenticationService와 동일한 패턴
 */
public interface AdminAuthenticationService {

    /**
     * 관리자 인증 처리
     */
    AdminLoginResponse authenticate(String email, String password);

    /**
     * 세션용 관리자 조회
     */
    AdminLoginResponse findAdminForSession(Long adminId);
}