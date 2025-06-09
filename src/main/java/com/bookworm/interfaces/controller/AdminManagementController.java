package com.bookworm.interfaces.controller;

import com.bookworm.application.dto.AdminSignUpResponse;
import com.bookworm.application.service.admin.AdminSignUpService;
import com.bookworm.interfaces.common.ApiResponse;
import com.bookworm.interfaces.common.ResponseHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 관리자 관리 API 컨트롤러
 * 관리자 조회, 수정, 삭제 등 관리 기능을 담당
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminManagementController {

    private final AdminSignUpService adminSignUpService;
    private final ResponseHelper responseHelper;

    /**
     * 모든 관리자 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminSignUpResponse>>> getAllAdmins() {
        log.info("모든 관리자 목록 조회 요청");

        List<AdminSignUpResponse> admins = adminSignUpService.findAllAdmins();

        return ResponseEntity.ok(
                ApiResponse.success(admins, "관리자 목록을 성공적으로 조회했습니다.")
        );
    }

    /**
     * 특정 관리자 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminSignUpResponse>> getAdminById(@PathVariable Long id) {
        log.info("관리자 조회 요청: ID={}", id);

        AdminSignUpResponse admin = adminSignUpService.findAdminById(id);

        return ResponseEntity.ok(
                ApiResponse.success(admin, "관리자 정보를 성공적으로 조회했습니다.")
        );
    }
}