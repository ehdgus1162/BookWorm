package com.bookworm.application.dto;

import com.bookworm.domain.entity.User;

/**
 * 관리자 회원가입/조회 응답 DTO
 */
public record AdminSignUpResponse(
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
     * User 엔티티(관리자)로부터 AdminSignUpResponse 객체 생성
     */
    public static AdminSignUpResponse from(User admin) {
        if (!admin.isAdmin()) {
            throw new IllegalArgumentException("관리자 계정이 아닙니다.");
        }

        return new AdminSignUpResponse(
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