package com.bookworm.interfaces.controller;

import com.bookworm.application.dto.AdminLoginRequest;
import com.bookworm.application.dto.AdminLoginResponse;
import com.bookworm.application.dto.AdminSignUpRequest;
import com.bookworm.application.dto.AdminSignUpResponse;
import com.bookworm.application.service.admin.AdminSignUpService;
import com.bookworm.interfaces.common.ApiResponse;
import com.bookworm.interfaces.common.ApiResponseHelper;
import com.bookworm.interfaces.common.AuthenticationValidator;
import com.bookworm.interfaces.common.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 리팩터링된 관리자 인증 관련 API 컨트롤러
 *
 * 개선사항:
 * 1. AuthController와 동일한 패턴으로 통일
 * 2. 공통 모듈 사용으로 코드 중복 제거
 * 3. 일관된 로깅 및 응답 형식 적용
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminSignUpService adminSignUpService;
    private final SessionManager sessionManager;
    private final AuthenticationValidator authValidator;

    /**
     * 관리자 회원가입 API
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AdminSignUpResponse>> signUp(
            @Valid @RequestBody AdminSignUpRequest request) {

        log.info("[관리자 회원가입] 요청 - 이메일: {}", request.email());

        AdminSignUpResponse createdAdmin = adminSignUpService.signUp(request);

        log.info("[관리자 회원가입] 성공 - 관리자 ID: {}", createdAdmin.id());

        return ApiResponseHelper.created(
                createdAdmin,
                "관리자가 성공적으로 생성되었습니다."
        );
    }

    /**
     * 관리자 로그인 API
     * 이메일/비밀번호 검증 후 관리자 전용 세션 생성
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletRequest httpRequest) {

        log.info("[관리자 로그인] 요청 - 이메일: {}", request.email());

        // 1. 관리자 인증 (서비스 계층에서 처리)
        AdminLoginResponse admin = adminSignUpService.authenticate(request.email(), request.password());

        // 2. 관리자 세션 관리 (공통 모듈로 분리)
        sessionManager.createAdminSession(httpRequest, admin);

        log.info("[관리자 로그인] 성공 - 관리자 ID: {}", admin.id());

        return ApiResponseHelper.ok(admin, "관리자 로그인에 성공했습니다.");
    }

    /**
     * 관리자 로그아웃 API
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        log.info("[관리자 로그아웃] 요청");

        boolean sessionInvalidated = sessionManager.invalidateSession(request);

        if (sessionInvalidated) {
            log.info("[관리자 로그아웃] 성공");
            return ApiResponseHelper.ok("관리자 로그아웃되었습니다.");
        } else {
            log.info("[관리자 로그아웃] 요청 - 기존 세션 없음");
            return ApiResponseHelper.ok("이미 로그아웃된 상태입니다.");
        }
    }

    /**
     * 현재 로그인한 관리자 정보 조회 API
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> getCurrentAdmin(HttpServletRequest request) {
        log.debug("[관리자 정보 조회] 요청");

        // 1. 관리자 인증 상태 검증 (공통 모듈로 분리)
        Long adminId = authValidator.validateAdminAuthentication(request);

        // 2. 최신 관리자 정보 조회
        AdminLoginResponse admin = adminSignUpService.findAdminForSession(adminId);

        log.debug("[관리자 정보 조회] 성공 - 관리자 ID: {}", adminId);

        return ApiResponseHelper.ok(admin, "관리자 정보를 성공적으로 조회했습니다.");
    }

    /**
     * 관리자 이메일 중복 확인 API
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(
            @RequestParam String email) {

        log.info("[관리자 이메일 중복 확인] 요청 - 이메일: {}", email);

        boolean exists = adminSignUpService.existsByEmail(email);

        String message = exists ? "이미 사용 중인 관리자 이메일입니다." : "사용 가능한 이메일입니다.";

        log.info("[관리자 이메일 중복 확인] 완료 - 이메일: {}, 존재 여부: {}", email, exists);

        return ApiResponseHelper.ok(exists, message);
    }
}