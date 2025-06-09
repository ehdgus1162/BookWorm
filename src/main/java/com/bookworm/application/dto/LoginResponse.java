package com.bookworm.application.dto;

import com.bookworm.domain.entity.User;

/**
 * 로그인 성공 응답 DTO
 * 로그인 후 클라이언트에게 필요한 기본 사용자 정보 포함
 *
 * 개선사항:
 * - role 필드 추가로 관리자/일반 사용자 구분 가능
 * - 프론트엔드에서 역할 기반 라우팅 지원
 */
public record LoginResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        String role,     // 추가: 사용자 역할 (ADMIN, USER)
        String status
) {

    /**
     * User 엔티티로부터 LoginResponse 객체 생성
     */
    public static LoginResponse from(User user) {
        return new LoginResponse(
                user.getId(),
                user.getEmail().getValue(),
                user.getFirstName().getValue(),
                user.getLastName().getValue(),
                user.getFullName(), // User 엔티티의 getFullName() 메서드 활용
                user.getRole().name(), // 역할 추가 (ADMIN, USER)
                user.getStatus().name()
        );
    }

    /**
     * 관리자 여부 확인
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /**
     * 일반 사용자 여부 확인
     */
    public boolean isUser() {
        return "USER".equals(role);
    }
}