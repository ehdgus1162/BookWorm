package com.bookworm.application.dto;

import com.bookworm.domain.entity.User;

/**
 * 관리자 로그인 성공 응답 DTO
 * User 엔티티의 관리자 데이터를 관리자용으로 변환
 */
public record AdminLoginResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        String role,
        String status,
        String lastLoginDate
) {

    /**
     * User 엔티티(관리자)로부터 AdminLoginResponse 객체 생성
     */
    public static AdminLoginResponse from(User admin) {
        if (!admin.isAdmin()) {
            throw new IllegalArgumentException("관리자 계정이 아닙니다.");
        }

        return new AdminLoginResponse(
                admin.getId(),
                admin.getEmail().getValue(),
                admin.getFirstName().getValue(),
                admin.getLastName().getValue(),
                admin.getFullName(),
                admin.getRole().name(),
                admin.getStatus().name(),
                admin.getLastLoginDate() != null ?
                        admin.getLastLoginDate().toString() : null
        );
    }
}