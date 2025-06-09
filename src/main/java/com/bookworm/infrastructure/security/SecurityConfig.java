package com.bookworm.infrastructure.security;

import com.bookworm.interfaces.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 통합 보안 설정
 * - 일반 사용자와 관리자를 모두 지원하는 통합 보안 구성
 * - API 요청과 웹 요청을 구분하여 처리
 * - 역할 기반 접근 제어 (RBAC) 적용
 * - CORS 설정 추가로 프론트엔드 통신 허용
 * - 새로운 반납 API 경로 지원 추가
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CORS 설정 - 가장 먼저 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 요청 권한 설정
                .authorizeHttpRequests(this::configureAuthorization)

                // 🔥 폼 로그인을 API용으로 커스터마이징 (비활성화하지 않음)
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")  // API 엔드포인트
                        .usernameParameter("email")             // JSON의 email 필드
                        .passwordParameter("password")          // JSON의 password 필드
                        .successHandler(loginSuccessHandler)    // 성공 핸들러
                        .failureHandler(loginFailureHandler)    // 실패 핸들러
                        .permitAll()
                )

                // HTTP Basic 인증 비활성화
                .httpBasic(basic -> basic.disable())

                // 로그아웃 설정 (API 기반)
                .logout(this::configureLogout)

                // 예외 처리 설정
                .exceptionHandling(this::configureExceptionHandling)

                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )

                // 기타 보안 설정
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")  // H2 Console CSRF 제외
                        .disable()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .build();
    }

    /**
     * CORS 설정 - Spring Security용
     * WebConfig의 설정과 동일하게 구성하되, Security에서 인식할 수 있도록 별도 Bean으로 정의
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 출처 (Origin) 설정
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",  // Vite 기본 포트 (Svelte)
                "http://localhost:5175",  // 현재 포트 추가
                "http://localhost:3000",  // 추가 개발 포트
                "http://localhost:8080"   // 같은 포트에서도 허용
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));

        // 허용할 헤더 (모든 헤더 허용)
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 자격 증명(쿠키, 인증 헤더 등) 허용
        configuration.setAllowCredentials(true);

        // Preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        // 노출할 헤더 (클라이언트에서 접근 가능한 헤더)
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With"
        ));

        // URL 패턴별로 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // 모든 경로에 적용

        return source;
    }

    /**
     * 요청 권한 설정
     * 공개 경로, 인증 경로, 역할별 API 접근 권한을 정의
     */
    private void configureAuthorization(org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                // === OPTIONS 요청 허용 (CORS Preflight) ===
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // === 공개 접근 허용 ===
                .requestMatchers(getPublicPaths()).permitAll()

                // === 인증 관련 API (로그인 전 접근 가능) ===
                .requestMatchers("/api/auth/**").permitAll()

                // ✅ 관리자 인증 관련 API를 먼저 처리 (순서 중요!)
                .requestMatchers("/api/admin/auth/signup").permitAll()
                .requestMatchers("/api/admin/auth/login").permitAll()
                .requestMatchers("/api/admin/auth/check-email").permitAll()

                // === 도서 관련 API ===
                // 조회는 모든 인증된 사용자 허용
                .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers(HttpMethod.POST, "/api/books/search").hasAnyRole("ADMIN", "USER")

                // 생성/수정/삭제는 관리자만 허용
                .requestMatchers(HttpMethod.POST, "/api/books").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/books/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")

                // === 대출 관련 API ===
                // 모든 대출 관련 API는 인증된 사용자만 접근 가능
                .requestMatchers("/api/loans/**").hasAnyRole("ADMIN", "USER")

                // ✅ === 새로운 반납 관련 API ===
                // 개발 환경에서는 모든 사용자 허용, 운영 환경에서는 인증 필요
                .requestMatchers("/api/returns/**").permitAll()  // 개발용 - 필요시 hasAnyRole("ADMIN", "USER")로 변경

                // === 사용자별 전용 API ===
                // ✅ 관리자 인증 API는 이미 위에서 처리했으므로, 나머지만 권한 체크
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasRole("USER")

                // === 웹 페이지 접근 제어 ===
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasRole("USER")

                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated();
    }

    /**
     * 공개 접근이 허용되는 경로들
     */
    private String[] getPublicPaths() {
        return new String[]{
                // 기본 경로
                "/", "/home", "/about",

                // 정적 리소스
                "/css/**", "/js/**", "/images/**", "/favicon.ico",
                "/static/**", "/assets/**",  // ✅ 프론트엔드 정적 파일 추가

                // 공개 웹 페이지
                "/login", "/signup",

                // 헬스체크 및 모니터링
                "/api/health", "/actuator/health",

                // ✅ 개발용 API 문서 (운영에서는 제거 권장)
                "/swagger-ui/**", "/v3/api-docs/**",

                // 개발용 (운영에서는 제거)
                "/h2-console/**"
        };
    }

    /**
     * 로그아웃 설정 (API 기반)
     */
    private void configureLogout(org.springframework.security.config.annotation.web.configurers.LogoutConfigurer<HttpSecurity> logout) {
        logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    log.info("사용자 로그아웃 완료: {}",
                            authentication != null ? authentication.getName() : "unknown");

                    try {
                        ApiResponse<Void> apiResponse = ApiResponse.success(null, "로그아웃이 완료되었습니다.");

                        response.setStatus(HttpStatus.OK.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                    } catch (Exception e) {
                        log.error("로그아웃 응답 작성 실패", e);
                        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    }
                })
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll();
    }

    /**
     * 예외 처리 설정
     * API 요청과 웹 요청을 구분하여 적절한 응답 제공
     */
    private void configureExceptionHandling(org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer<HttpSecurity> exceptions) {
        exceptions
                // 인증되지 않은 API 요청 처리
                .authenticationEntryPoint((request, response, authException) -> {
                    String requestURI = request.getRequestURI();

                    if (isApiRequest(requestURI)) {
                        // API 요청 - JSON 응답
                        log.warn("인증되지 않은 API 접근 시도: {} {}", request.getMethod(), requestURI);

                        try {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");

                            ApiResponse<Void> apiResponse = ApiResponse.error(401, "인증이 필요합니다.");
                            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                        } catch (Exception e) {
                            log.error("인증 오류 응답 작성 실패", e);
                        }
                    } else {
                        // 웹 요청 - 로그인 페이지로 리다이렉트
                        log.debug("인증되지 않은 웹 페이지 접근: {} {}", request.getMethod(), requestURI);
                        try {
                            response.sendRedirect("/login");
                        } catch (Exception e) {
                            log.error("로그인 페이지 리다이렉트 실패", e);
                        }
                    }
                })

                // 권한 부족 처리
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String requestURI = request.getRequestURI();

                    if (isApiRequest(requestURI)) {
                        // API 요청 - JSON 응답
                        log.warn("권한 부족 API 접근 시도: {} {} by {}",
                                request.getMethod(), requestURI, request.getRemoteUser());

                        try {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");

                            ApiResponse<Void> apiResponse = ApiResponse.error(403, "접근 권한이 없습니다.");
                            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                        } catch (Exception e) {
                            log.error("권한 오류 응답 작성 실패", e);
                        }
                    } else {
                        // 웹 요청 - 접근 거부 페이지로 리다이렉트
                        log.warn("권한 부족 웹 페이지 접근: {} {} by {}",
                                request.getMethod(), requestURI, request.getRemoteUser());
                        try {
                            response.sendRedirect("/access-denied");
                        } catch (Exception e) {
                            log.error("접근 거부 페이지 리다이렉트 실패", e);
                        }
                    }
                });
    }

    /**
     * API 요청인지 확인
     */
    private boolean isApiRequest(String requestURI) {
        return requestURI != null && requestURI.startsWith("/api/");
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 비밀번호 인코더 빈 등록
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}