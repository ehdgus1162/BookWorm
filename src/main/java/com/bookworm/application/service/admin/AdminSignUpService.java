package com.bookworm.application.service.admin;

import com.bookworm.application.dto.AdminLoginResponse;
import com.bookworm.application.dto.AdminSignUpRequest;
import com.bookworm.application.dto.AdminSignUpResponse;

import java.util.List;

/**
 * 관리자 관련 통합 서비스 인터페이스
 * 기존 UserSignUpService와 동일한 패턴
 */
public interface AdminSignUpService {

    // 등록 관련
    AdminSignUpResponse signUp(AdminSignUpRequest request);

    // 조회 관련
    List<AdminSignUpResponse> findAllAdmins();
    AdminSignUpResponse findAdminById(Long id);

    // 검증 관련
    boolean existsByEmail(String email);

    // 인증 관련
    AdminLoginResponse authenticate(String email, String password);
    AdminLoginResponse findAdminForSession(Long adminId);
}