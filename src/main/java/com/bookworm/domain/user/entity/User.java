package com.bookworm.domain.user.entity;

import com.bookworm.domain.user.constant.Role;
import com.bookworm.domain.vo.value.Email;
import com.bookworm.domain.vo.value.FullName;
import com.bookworm.domain.vo.value.Password;
import com.bookworm.infrastructure.jpa.BaseUserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUserEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * 빌더 패턴을 활용한 생성자
     *
     * @param name 사용자 이름
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @param role 사용자 역할
     */
    @Builder
    public User(FullName name, Email email, Password password, Role role) {
        super(name, email, password);
        this.role = role;
    }

    /**
     * 사용자 생성 팩토리 메서드
     *
     * @param lastName 성
     * @param firstName 이름
     * @param email 이메일
     * @param password 비밀번호
     * @param role 사용자 역할
     * @return 새로운 사용자 엔티티
     */
    public static User of(String lastName, String firstName, String email, String password, Role role) {
        return User.builder()
                .name(FullName.of(lastName, firstName))
                .email(Email.of(email))
                .password(Password.of(password))
                .role(role)
                .build();
    }

    /**
     * 암호화된 비밀번호로 사용자 생성
     *
     * @param lastName 성
     * @param firstName 이름
     * @param email 이메일
     * @param encryptedPassword 암호화된 비밀번호
     * @param role 사용자 역할
     * @return 새로운 사용자 엔티티
     */
    public static User ofWithEncryptedPassword(String lastName, String firstName, String email,
                                               String encryptedPassword, Role role) {
        return User.builder()
                .name(FullName.of(lastName, firstName))
                .email(Email.of(email))
                .password(Password.ofEncrypted(encryptedPassword))
                .role(role)
                .build();
    }

    /**
     * 암호화된 비밀번호로 사용자 업데이트
     *
     * @param encryptedPassword 암호화된 비밀번호
     * @return 업데이트된 사용자 엔티티
     */
    public User withEncryptedPassword(String encryptedPassword) {
        return User.builder()
                .name(this.getName())
                .email(this.getEmail())
                .password(Password.ofEncrypted(encryptedPassword))
                .role(this.role)
                .build();
    }
}
