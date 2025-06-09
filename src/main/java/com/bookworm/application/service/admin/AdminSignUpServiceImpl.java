package com.bookworm.application.service.admin;


import com.bookworm.application.dto.AdminLoginResponse;
import com.bookworm.application.dto.AdminSignUpRequest;
import com.bookworm.application.dto.AdminSignUpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자 관련 통합 서비스 구현체
 * 기존 UserSignUpServiceImpl과 동일한 파사드 패턴
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminSignUpServiceImpl implements AdminSignUpService {

    private final AdminRegistrationService adminRegistrationService;
    private final AdminQueryService adminQueryService;
    private final AdminValidationService adminValidationService;
    private final AdminAuthenticationService adminAuthenticationService;

    @Override
    public AdminSignUpResponse signUp(AdminSignUpRequest request) {
        return adminRegistrationService.signUp(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminSignUpResponse> findAllAdmins() {
        return adminQueryService.findAllAdmins();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminSignUpResponse findAdminById(Long id) {
        return adminQueryService.findAdminById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return adminValidationService.existsByEmail(email);
    }

    // ========== 인증 관련 메서드 구현 ==========

    @Override
    @Transactional(readOnly = true)
    public AdminLoginResponse authenticate(String email, String password) {
        log.info("통합 서비스를 통한 관리자 인증 요청: {}", email);
        return adminAuthenticationService.authenticate(email, password);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminLoginResponse findAdminForSession(Long adminId) {
        log.debug("통합 서비스를 통한 세션 관리자 조회: 관리자 ID={}", adminId);
        return adminAuthenticationService.findAdminForSession(adminId);
    }
}