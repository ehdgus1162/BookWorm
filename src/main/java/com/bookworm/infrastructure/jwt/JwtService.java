package com.bookworm.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final long EXPIRATION_TIME = 86_400_000; // 1일
    private static final String PREFIX = "Bearer ";
    private static final String HEADER_STRING = "Authorization";

    // 애플리케이션 시작 시 한 번만 생성
    private static final Key KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

    /**
     * username을 담은 JWT 토큰 생성
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY)
                .compact();
    }

    /**
     * HTTP 요청 헤더에서 토큰을 추출해 Subject(username)를 반환
     * @return username, 토큰이 없거나 유효하지 않으면 null 반환
     */
    public String getAuthUser(HttpServletRequest request) {
        String header = request.getHeader(HEADER_STRING);
        if (header == null || !header.startsWith(PREFIX)) {
            return null;
        }

        String token = header.substring(PREFIX.length());
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token);

            // (선택) 만료 검사: parseClaimsJws가 자동으로 만료 시 예외를 던집니다.
            return claims.getBody().getSubject();

        } catch (Exception e) {
            // 토큰 파싱 실패(형식 오류, 서명 불일치, 만료 등)
            return null;
        }
    }
}
