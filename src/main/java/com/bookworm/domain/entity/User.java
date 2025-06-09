package com.bookworm.domain.entity;

import com.bookworm.domain.common.AuditableBaseEntity;
import com.bookworm.domain.common.TimeProvider;
import com.bookworm.domain.constant.Role;
import com.bookworm.domain.constant.UserStatus;
import com.bookworm.domain.vo.user.*;
import com.bookworm.domain.exception.UserBusinessException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class User extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Embedded
    private Password password;

    @Embedded
    private FirstName firstName;

    @Embedded
    private LastName lastName;

    @Embedded
    private Address address;

    @Embedded
    private PhoneNumber phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Version
    private Long version;

    /**
     * 새로운 일반 사용자 생성
     */
    public static User create(Email email, String rawPassword, FirstName firstName,
                              LastName lastName, Address address, PhoneNumber phoneNumber,
                              PasswordEncoder passwordEncoder) {
        validateCreateInputs(email, rawPassword, firstName, lastName);

        Password encryptedPassword = Password.of(rawPassword, passwordEncoder);

        return User.of()
                .email(email)
                .password(encryptedPassword)
                .firstName(firstName)
                .lastName(lastName)
                .address(address)
                .phoneNumber(phoneNumber)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    /**
     * 새로운 관리자 사용자 생성
     */
    public static User createAdmin(Email email, String rawPassword, FirstName firstName,
                                   LastName lastName, Address address, PhoneNumber phoneNumber,
                                   PasswordEncoder passwordEncoder) {
        validateCreateInputs(email, rawPassword, firstName, lastName);

        Password encryptedPassword = Password.of(rawPassword, passwordEncoder);

        return User.of()
                .email(email)
                .password(encryptedPassword)
                .firstName(firstName)
                .lastName(lastName)
                .address(address)        // ← Address 추가
                .phoneNumber(phoneNumber) // ← PhoneNumber 추가
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private static void validateCreateInputs(Email email, String rawPassword,
                                             FirstName firstName, LastName lastName) {
        if (email == null) {
            throw new UserBusinessException("이메일은 필수입니다.");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new UserBusinessException("비밀번호는 필수입니다.");
        }
        if (firstName == null) {
            throw new UserBusinessException("이름은 필수입니다.");
        }
        if (lastName == null) {
            throw new UserBusinessException("성은 필수입니다.");
        }
    }

    // 핵심 비즈니스 메서드만 유지

    /**
     * 사용자 비활성화
     */
    public void deactivate() {
        if (status == UserStatus.INACTIVE) {
            throw new UserBusinessException("이미 비활성화된 사용자입니다.");
        }
        this.status = UserStatus.INACTIVE;
    }

    /**
     * 사용자 활성화
     */
    public void activate() {
        if (status == UserStatus.ACTIVE) {
            throw new UserBusinessException("이미 활성화된 사용자입니다.");
        }
        this.status = UserStatus.ACTIVE;
    }

    /**
     * 역할 변경
     */
    public void changeRole(Role newRole) {
        if (newRole == null) {
            throw new UserBusinessException("역할은 필수입니다.");
        }
        this.role = newRole;
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(String oldRawPassword, String newRawPassword,
                               PasswordEncoder passwordEncoder) {
        if (!this.password.matches(oldRawPassword, passwordEncoder)) {
            throw new UserBusinessException("기존 비밀번호가 일치하지 않습니다.");
        }

        this.password = Password.of(newRawPassword, passwordEncoder);
    }

    /**
     * 로그인 성공 기록
     */
    public void recordLogin(TimeProvider timeProvider) {
        this.lastLoginDate = timeProvider.currentDateTime();
    }

    /**
     * 활성 상태 확인
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    /**
     * 관리자 권한 확인
     */
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    /**
     * 전체 이름 반환
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "";
        }
        if (firstName == null) {
            return lastName.getValue();
        }
        if (lastName == null) {
            return firstName.getValue();
        }
        return firstName.getValue() + " " + lastName.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id == null || user.id == null) {
            return false;
        }

        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, email='%s', role=%s, status=%s}",
                id, email != null ? email.getValue() : "null", role, status);
    }
}