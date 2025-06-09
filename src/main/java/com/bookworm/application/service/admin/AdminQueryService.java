package com.bookworm.application.service.admin;

import com.bookworm.application.dto.AdminSignUpResponse;
import com.bookworm.domain.entity.User;

import java.util.List;

/**
 * 관리자 조회 서비스 인터페이스
 */
public interface AdminQueryService {

    List<AdminSignUpResponse> findAllAdmins();
    AdminSignUpResponse findAdminById(Long id);
    User getAdminEntityById(Long id);
}