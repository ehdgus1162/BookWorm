package com.bookworm.application.service.common;

import com.bookworm.application.dto.SignUpRequest;
import com.bookworm.domain.constant.Role;
import com.bookworm.domain.constant.UserStatus;
import com.bookworm.domain.entity.User;
import com.bookworm.domain.vo.user.*;
import org.springframework.stereotype.Component;

@Component
public class UserCreationService {

    /**
     * 새 사용자 생성
     */
    public User createUser(SignUpRequest req, Password password) {
        return User.of()
                .email(Email.of(req.email()))
                .password(password)
                .firstName(new FirstName(req.firstName()))
                .lastName(new LastName(req.lastName()))
                .address(new Address(req.street(), req.city(), req.state(), req.country()))
                .phoneNumber(new PhoneNumber(req.phoneNumber()))
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    /**
     * 기존 사용자 정보 업데이트
     */
    public User updateUser(User existingUser, SignUpRequest req, Password password) {
        return User.of()
                .id(existingUser.getId())
                .email(Email.of(req.email()))
                .password(password)
                .firstName(new FirstName(req.firstName()))
                .lastName(new LastName(req.lastName()))
                .address(new Address(req.street(), req.city(), req.state(), req.country()))
                .phoneNumber(new PhoneNumber(req.phoneNumber()))
                .role(existingUser.getRole())  // 기존 역할 유지
                .status(existingUser.getStatus())  // 기존 상태 유지
                .build();
    }

    /**
     * 사용자 비활성화 (논리적 삭제)
     */
    public User deactivateUser(User existingUser) {
        return User.of()
                .id(existingUser.getId())
                .email(existingUser.getEmail())
                .password(existingUser.getPassword())
                .firstName(existingUser.getFirstName())
                .lastName(existingUser.getLastName())
                .address(existingUser.getAddress())
                .phoneNumber(existingUser.getPhoneNumber())
                .role(existingUser.getRole())
                .status(UserStatus.INACTIVE)  // 비활성화 상태로 변경
                .build();
    }
}