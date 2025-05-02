package com.bookworm.infrastructure.jpa;

import com.bookworm.domain.user.constant.UserStatus;
import com.bookworm.domain.vo.value.Email;
import com.bookworm.domain.vo.value.FullName;
import com.bookworm.domain.vo.value.Password;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 순수 JPA 매핑 전용 USER 클래스
 */
@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseUserEntity extends BaseTimeEntity {



    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lastName.value", column = @Column(name = "last_name", nullable = false)),
            @AttributeOverride(name = "firstName.value", column = @Column(name = "first_name", nullable = false))
    })
    private FullName name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true))
    private Email email;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "password", nullable = false)),
            @AttributeOverride(name = "encrypted", column = @Column(name = "is_password_encrypted", nullable = false))
    })
    private Password password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // 공통 생성자
    protected BaseUserEntity(FullName name, Email email, Password password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.status = UserStatus.ACTIVE;
    }

    public void markLogin(LocalDateTime when) {
        this.lastLoginAt = when;
    }
}
