package com.bookworm.domain.user.service;

import com.bookworm.common.error.ErrorMessage;
import com.bookworm.common.policy.PasswordEncoder;
import com.bookworm.domain.user.constant.Role;
import com.bookworm.domain.user.entity.User;
import com.bookworm.domain.user.exception.UserInvalidPasswordException;
import com.bookworm.domain.user.exception.UserNotFoundException;
import com.bookworm.domain.user.repository.UserRepository;
import com.bookworm.domain.vo.value.Email;
import com.bookworm.domain.vo.value.FullName;
import com.bookworm.domain.vo.value.Password;
import org.springframework.transaction.annotation.Transactional;

public class UserDomainService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDomainService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * 사용자 등록 메서드
     *

     * @return 등록된 사용자 ID
     */

    /**
     * 사용자 등록 메서드
     *
     * @param lastName 성
     * @param firstName 이름
     * @param email 이메일
     * @param rawPassword 평문 비밀번호
     * @param role 사용자 역할
     * @return 등록된 사용자
     */
    @Transactional
    public User registerUser(String lastName, String firstName, String email, String rawPassword, Role role) {
        // 유효성 검증 수행 (값 객체 생성 과정에서 자동으로 수행됨)
        Password validPassword = Password.of(rawPassword);

        // 비밀번호 암호화
        String encryptedValue = passwordEncoder.encrypt(validPassword.getValue());

        // 검증된 값과 암호화된 비밀번호로 사용자 생성
        User user = User.builder()
                .name(FullName.of(lastName, firstName))
                .email(Email.of(email))
                .password(Password.ofEncrypted(encryptedValue))
                .role(role)
                .build();

        // 저장 및 반환
        return userRepository.save(user);
    }

    /**
     * 비밀번호 변경
     *
     * @param userId 사용자 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     * @return 업데이트된 사용자
     */
    @Transactional
    public User changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.User.NOT_FOUND_BY_ID));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword().getValue())) {
            throw new UserInvalidPasswordException(ErrorMessage.User.INVALID_PASSWORD);
        }

        // 새 비밀번호 유효성 검증
        Password validNewPassword = Password.of(newPassword);

        // 비밀번호 암호화 및 사용자 업데이트
        String encryptedNewPassword = passwordEncoder.encrypt(validNewPassword.getValue());
        User updatedUser = user.withEncryptedPassword(encryptedNewPassword);

        return userRepository.save(updatedUser);
    }
}
