package com.bookworm.domain.vo.user;

import com.bookworm.domain.validation.utils.PasswordValidationUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Embeddable
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class Password {
    @Column(name = "password", nullable = false)
    private String value;

    /**
     * 일반 생성자 - 이미 암호화된 비밀번호를 저장할 때 사용
     */
    public Password(String encryptedPassword) {
        this.value = encryptedPassword;
    }

    /**
     * 팩토리 메서드 - 평문 비밀번호를 검증하고 암호화하여 Password 객체 생성
     */
    public static Password of(String rawPassword, PasswordEncoder passwordEncoder) {
        PasswordValidationUtils.validatePasswordWithException(rawPassword);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        return new Password(encodedPassword);
    }

    /**
     * 비밀번호 일치 여부 확인
     */
    public boolean matches(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, this.value);
    }
}