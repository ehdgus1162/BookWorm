package com.bookworm.application.service.admin;

import com.bookworm.application.dto.AdminSignUpRequest;
import com.bookworm.application.dto.AdminSignUpResponse;

/**
 * 관리자 등록 서비스 인터페이스
 */
public interface AdminRegistrationService {

    /**
     * 관리자 회원가입
     */
    AdminSignUpResponse signUp(AdminSignUpRequest request);
}