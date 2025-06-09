package com.bookworm.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bookworm.interfaces.common.ApiResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 세션 기반 인증 필터 (비활성화)
 * Spring Security를 사용하므로 이 필터는 비활성화합니다.
 *
 * 활성화하려면 application.yml에 다음 설정 추가:
 * custom.security.session-filter.enabled: true
 */
@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(
        value = "custom.security.session-filter.enabled",
        havingValue = "true",
        matchIfMissing = false  // 기본값: 비활성화
)
public class SessionAuthenticationFilter implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 인증이 필요한 API 경로들 (참고용)
    private static final Set<String> PROTECTED_API_PATHS = Set.of(
            "/api/users",           // 사용자 관리 API
            "/api/auth/me"          // 현재 사용자 정보 조회
    );

    // 인증이 필요한 웹 페이지 경로들 (참고용)
    private static final Set<String> PROTECTED_WEB_PATHS = Set.of(
            "/web/dashboard",       // 대시보드
            "/web/profile",         // 프로필 페이지
            "/web/settings"         // 설정 페이지
    );

    // 인증이 필요하지 않은 경로들 (참고용)
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/signup",     // 회원가입
            "/api/auth/login",      // 로그인
            "/api/auth/logout",     // 로그아웃
            "/api/auth/check-email", // 이메일 중복 확인
            "/web/login",           // 로그인 페이지
            "/web/signup",          // 회원가입 페이지
            "/css/",                // 정적 리소스
            "/js/",
            "/images/",
            "/favicon.ico"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        log.debug("SessionAuthenticationFilter가 활성화되어 있습니다. Spring Security와 충돌할 수 있습니다.");

        // 현재는 그냥 통과시킴 (Spring Security가 인증 처리)
        chain.doFilter(request, response);
    }

    /**
     * 아래는 참고용 메서드들입니다.
     * Spring Security를 사용하지 않고 커스텀 세션 인증을 구현할 때 사용할 수 있습니다.
     */

    /**
     * 공개 경로인지 확인
     */
    private boolean isPublicPath(String requestURI) {
        return PUBLIC_PATHS.stream()
                .anyMatch(requestURI::startsWith);
    }

    /**
     * 보호된 경로인지 확인
     */
    private boolean isProtectedPath(String requestURI) {
        return PROTECTED_API_PATHS.stream().anyMatch(requestURI::startsWith) ||
                PROTECTED_WEB_PATHS.stream().anyMatch(requestURI::startsWith);
    }

    /**
     * 세션이 유효한지 확인
     */
    private boolean isValidSession(HttpSession session) {
        return session != null && session.getAttribute("userId") != null;
    }

    /**
     * 인증되지 않은 요청 처리
     */
    private void handleUnauthenticatedRequest(HttpServletRequest request,
                                              HttpServletResponse response,
                                              String requestURI) throws IOException {

        log.warn("인증되지 않은 접근 시도: {} {}", request.getMethod(), requestURI);

        if (requestURI.startsWith("/api/")) {
            // API 요청 - JSON 응답
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");

            ApiResponse<Void> apiResponse = ApiResponse.error("로그인이 필요합니다.");
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);

            response.getWriter().write(jsonResponse);
        } else {
            // 웹 페이지 요청 - 로그인 페이지로 리다이렉트
            String redirectUrl = "/web/login";
            if (!requestURI.equals("/web") && !requestURI.equals("/web/")) {
                redirectUrl += "?redirect=" + URLEncoder.encode(requestURI, StandardCharsets.UTF_8);
            }
            response.sendRedirect(redirectUrl);
        }
    }
}