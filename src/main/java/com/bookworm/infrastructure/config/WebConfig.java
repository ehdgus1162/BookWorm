package com.bookworm.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 설정 클래스
 * CORS 설정을 통해 Svelte 개발 서버와의 통신을 허용합니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 경로에 적용
                .allowedOriginPatterns("*")  // ⭐ allowedOrigins("*") 대신 사용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)  // 세션 쿠키 전송을 위해 필요
                .maxAge(3600); // preflight 캐시 시간 (1시간)
    }
}