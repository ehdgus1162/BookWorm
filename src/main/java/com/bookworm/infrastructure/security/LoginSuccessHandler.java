package com.bookworm.infrastructure.security;

import com.bookworm.domain.constant.Role;
import com.bookworm.domain.entity.User;
import com.bookworm.domain.repository.UserRepository;
import com.bookworm.domain.vo.user.Email;
import com.bookworm.interfaces.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 로그인 성공 핸들러
 * Spring Security 폼 로그인 성공 시 JSON 응답 반환
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        try {
            // 인증된 사용자 정보 가져오기
            String userEmail = authentication.getName();
            User user = findUserByEmail(userEmail);

            if (user == null) {
                handleError(response, "사용자 정보를 찾을 수 없습니다.");
                return;
            }

            Role userRole = user.getRole();
            String redirectUrl = determineRedirectUrl(userRole);

            // 응답 데이터 생성
            Map<String, Object> responseData = createSuccessResponse(user, redirectUrl);
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(
                    responseData,
                    "로그인이 성공적으로 완료되었습니다."
            );

            log.info("로그인 성공 - 사용자: {}, 역할: {}, ID: {}",
                    userEmail, userRole, user.getId());

            writeJsonResponse(response, apiResponse);

        } catch (Exception e) {
            log.error("로그인 성공 처리 중 오류 발생", e);
            handleError(response, "로그인 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 이메일로 사용자 조회
     */
    private User findUserByEmail(String email) {
        try {
            Email emailVo = Email.of(email);
            return userRepository.findByEmail(emailVo).orElse(null);
        } catch (Exception e) {
            log.error("사용자 조회 중 오류 발생: {}", email, e);
            return null;
        }
    }

    /**
     * 역할에 따른 리다이렉트 URL 결정
     */
    private String determineRedirectUrl(Role role) {
        return switch (role) {
            case ADMIN -> "/admin/dashboard";
            case USER -> "/dashboard";
        };
    }

    /**
     * 성공 응답 데이터 생성
     */
    private Map<String, Object> createSuccessResponse(User user, String redirectUrl) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail().getValue());
        response.put("name", user.getFullName());
        response.put("firstName", user.getFirstName().getValue());
        response.put("lastName", user.getLastName().getValue());
        response.put("role", user.getRole().name());
        response.put("status", user.getStatus().name());
        response.put("redirectUrl", redirectUrl);
        return response;
    }

    /**
     * JSON 응답 작성
     */
    private void writeJsonResponse(HttpServletResponse response, ApiResponse<?> apiResponse) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }

    /**
     * 오류 응답 처리
     */
    private void handleError(HttpServletResponse response, String message) throws IOException {
        ApiResponse<Void> errorResponse = ApiResponse.error(500, message);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    /**
     * 사용자 권한 확인 (참고용)
     */
    private boolean hasRole(Authentication authentication, Role role) {
        String authority = "ROLE_" + role.name();
        return authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority(authority));
    }
}