package com.bookworm.interfaces.controller;

import com.bookworm.application.dto.LoginRequest;
import com.bookworm.application.dto.LoginResponse;
import com.bookworm.application.dto.SignUpRequest;
import com.bookworm.application.dto.SignUpResponse;
import com.bookworm.application.service.user.UserSignUpService;
import com.bookworm.domain.entity.User;
import com.bookworm.domain.repository.UserRepository;
import com.bookworm.domain.vo.user.Email;
import com.bookworm.interfaces.common.ApiResponse;
import com.bookworm.interfaces.common.ApiResponseHelper;
import com.bookworm.interfaces.common.AuthenticationValidator;
import com.bookworm.interfaces.common.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 리팩터링된 통합 인증 API 컨트롤러
 *
 * 개선사항:
 * 1. 기존 아키텍처 유지 (SessionManager, AuthenticationValidator)
 * 2. Spring Security 통합 추가
 * 3. 일반 사용자와 관리자 통합 처리
 * 4. 역할 기반 로깅 및 응답
 * 5. 이중 인증 검증 (Spring Security + 기존 세션)
 * 6. CORS 설정은 WebConfig에서 처리 (중복 제거)
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
// ⭐ @CrossOrigin 어노테이션 제거 - WebConfig에서 전역 처리
public class AuthController {

    private final UserSignUpService userSignUpService;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final AuthenticationValidator authValidator;

    /**
     * 회원가입 API
     * 일반 사용자 회원가입 처리
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
            @Valid @RequestBody SignUpRequest request) {

        log.info("[회원가입] 요청 - 이메일: {}", request.email());

        try {
            SignUpResponse createdUser = userSignUpService.signUp(request);

            log.info("[회원가입] 성공 - 사용자 ID: {}", createdUser.id());

            return ApiResponseHelper.created(
                    createdUser,
                    "사용자가 성공적으로 생성되었습니다."
            );
        } catch (Exception e) {
            log.error("[회원가입] 실패 - 이메일: {}, 오류: {}", request.email(), e.getMessage());
            return ApiResponseHelper.badRequest("회원가입에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 통합 로그인 API
     * 일반 사용자와 관리자 모두 처리하며 기존 아키텍처와 Spring Security 통합
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        log.info("[통합 로그인] 요청 - 이메일: {}", request.email());

        try {
            log.debug("[통합 로그인] 1단계 - UserSignUpService 인증 시작");

            // 1. 기존 UserSignUpService를 통한 인증
            LoginResponse loginResponse = userSignUpService.authenticate(request.email(), request.password());

            log.debug("[통합 로그인] 2단계 - User 엔티티 조회 시작");

            // 2. User 엔티티 조회 (Spring Security Context 설정용)
            User user = findUserByEmail(request.email());

            log.debug("[통합 로그인] 3단계 - Spring Security Context 설정 시작");

            // 3. Spring Security Context 설정
            setSecurityContext(user, httpRequest);

            log.debug("[통합 로그인] 4단계 - 세션 관리자 세션 생성 시작");

            // 4. 기존 세션 관리자를 통한 세션 생성
            sessionManager.createUserSession(httpRequest, loginResponse);

            log.debug("[통합 로그인] 5단계 - 응답 생성 시작");

            // 5. 역할별 성공 로그
            logSuccessfulLogin(loginResponse);

            return ApiResponseHelper.ok(loginResponse, "로그인에 성공했습니다.");

        } catch (Exception e) {
            log.error("[통합 로그인] 실패 - 이메일: {}, 오류 클래스: {}, 오류 메시지: {}",
                    request.email(), e.getClass().getSimpleName(), e.getMessage());
            log.error("[통합 로그인] 스택 트레이스: ", e);
            return ApiResponseHelper.badRequest("로그인에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 로그아웃 API
     * Spring Security Context와 기존 세션을 모두 정리
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        log.info("[로그아웃] 요청");

        try {
            // 1. Spring Security Context 정리
            SecurityContextHolder.clearContext();

            // 2. 기존 SessionManager를 통한 세션 무효화
            boolean sessionInvalidated = sessionManager.invalidateSession(request);

            if (sessionInvalidated) {
                log.info("[로그아웃] 성공 - 세션 무효화 완료");
                return ApiResponseHelper.ok("로그아웃되었습니다.");
            } else {
                log.info("[로그아웃] 요청 - 기존 세션 없음");
                return ApiResponseHelper.ok("이미 로그아웃된 상태입니다.");
            }
        } catch (Exception e) {
            log.error("[로그아웃] 오류 발생", e);
            return ApiResponseHelper.error(HttpStatus.INTERNAL_SERVER_ERROR, "로그아웃 중 오류가 발생했습니다.");
        }
    }

    /**
     * 현재 로그인한 사용자 정보 조회 API
     * Spring Security Context와 기존 세션 검증을 모두 활용
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse>> getCurrentUser(HttpServletRequest request) {
        log.debug("[사용자 정보 조회] 요청");

        try {
            // 1. Spring Security 인증 상태 우선 확인
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() &&
                    !"anonymousUser".equals(authentication.getPrincipal())) {

                // Spring Security Context에서 사용자 정보 추출
                String userEmail = authentication.getName();
                User user = findUserByEmail(userEmail);
                LoginResponse response = LoginResponse.from(user);

                log.debug("[사용자 정보 조회] 성공 (Spring Security) - 사용자: {}", userEmail);
                return ApiResponseHelper.ok(response, "사용자 정보를 성공적으로 조회했습니다.");
            }

            // 2. 기존 세션 기반 검증으로 폴백
            try {
                Long userId = authValidator.validateUserAuthentication(request);
                LoginResponse user = userSignUpService.findUserForSession(userId);

                log.debug("[사용자 정보 조회] 성공 (세션 기반) - 사용자 ID: {}", userId);
                return ApiResponseHelper.ok(user, "사용자 정보를 성공적으로 조회했습니다.");

            } catch (Exception e) {
                return ApiResponseHelper.unauthorized("인증이 필요합니다.");
            }

        } catch (Exception e) {
            log.error("[사용자 정보 조회] 실패", e);
            return ApiResponseHelper.unauthorized("인증된 사용자가 아닙니다.");
        }
    }

    /**
     * 이메일 중복 확인 API
     * 기존 로직 유지
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(
            @RequestParam String email) {

        log.info("[이메일 중복 확인] 요청 - 이메일: {}", email);

        try {
            boolean exists = userSignUpService.existsByEmail(email);
            String message = exists ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.";

            log.info("[이메일 중복 확인] 완료 - 이메일: {}, 존재 여부: {}", email, exists);
            return ApiResponseHelper.ok(exists, message);

        } catch (Exception e) {
            log.error("[이메일 중복 확인] 오류 - 이메일: {}", email, e);
            return ApiResponseHelper.badRequest("이메일 중복 확인 중 오류가 발생했습니다.");
        }
    }

    /**
     * 인증 API 헬스체크 (테스트용)
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        log.info("[인증 API] 헬스체크 요청");
        return ApiResponseHelper.ok("AuthController is working", "인증 API가 정상 작동 중입니다.");
    }

    // === Private Helper Methods ===

    /**
     * 이메일로 사용자 조회
     */
    private User findUserByEmail(String email) {
        try {
            Email emailVo = Email.of(email);
            return userRepository.findByEmail(emailVo)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        } catch (Exception e) {
            throw new RuntimeException("사용자 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * Spring Security Context 설정
     */
    private void setSecurityContext(User user, HttpServletRequest request) {
        try {
            // 권한 설정
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );

            // Authentication 객체 생성
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getEmail().getValue(),  // Principal: 사용자 이메일
                    null,                        // Credentials: 저장하지 않음
                    authorities                  // Authorities: 역할 기반 권한
            );

            // Security Context 설정
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            // 세션에 Security Context 저장
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            log.debug("[Security Context] 설정 완료 - 사용자: {}, 역할: {}",
                    user.getEmail().getValue(), user.getRole());

        } catch (Exception e) {
            log.error("[Security Context] 설정 실패", e);
            throw new RuntimeException("보안 컨텍스트 설정에 실패했습니다.", e);
        }
    }

    /**
     * 역할별 성공 로그 기록
     */
    private void logSuccessfulLogin(LoginResponse loginResponse) {
        if (loginResponse.isAdmin()) {
            log.info("[관리자 로그인] 성공 - ID: {}, 이름: {}, 이메일: {}",
                    loginResponse.id(), loginResponse.fullName(), loginResponse.email());
        } else {
            log.info("[사용자 로그인] 성공 - ID: {}, 이름: {}, 이메일: {}",
                    loginResponse.id(), loginResponse.fullName(), loginResponse.email());
        }
    }
}