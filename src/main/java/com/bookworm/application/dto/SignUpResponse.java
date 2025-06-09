package com.bookworm.application.dto;

import com.bookworm.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 * 민감한 정보(비밀번호)는 제외하고, 클라이언트에게 필요한 정보만 포함
 *
 * 회원가입 후 즉시 로그인:
 *
 * 회원가입과 동시에 사용자 ID를 반환받아 자동 로그인 처리가 가능합니다.
 *
 *
 * 계정 관리 페이지로 이동:
 *
 * 가입 후 생성된 사용자 정보를 바로 활용하여 프로필 페이지를 초기화할 수 있습니다.
 *
 *
 * 환영 이메일/메시지 전송:
 *
 * 클라이언트가 사용자 이름 등의 정보를 바로 활용하여 맞춤형 환영 메시지를 표시할 수 있습니다.
 */
public record SignUpResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        AddressResponse address,
        String phoneNumber,
        String status

){

    /**
     * User 엔티티로부터 UserResponse 객체 생성
     */
    public static SignUpResponse from(User user) {
        return new SignUpResponse(
                user.getId(),
                user.getEmail().getValue(),
                user.getFirstName().getValue(),
                user.getLastName().getValue(),
                user.getFirstName().getValue() + " " + user.getLastName().getValue(),
                new AddressResponse(
                        user.getAddress().getStreet(),
                        user.getAddress().getCity(),
                        user.getAddress().getState(),
                        user.getAddress().getCountry()
                ),
                user.getPhoneNumber().getValue(),
                user.getStatus().name()
        );
    }
}