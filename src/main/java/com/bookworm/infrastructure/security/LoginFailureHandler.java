package com.bookworm.infrastructure.security;

import com.bookworm.interfaces.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String userEmail = request.getParameter("email");
        log.warn("로그인 실패 - 사용자: {}, 원인: {}", userEmail, exception.getMessage());

        ApiResponse<Void> apiResponse = ApiResponse.error(401, "이메일 또는 비밀번호가 올바르지 않습니다.");
        writeJsonResponse(response, apiResponse);
    }

    private void writeJsonResponse(HttpServletResponse response, ApiResponse<?> apiResponse) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }
}
