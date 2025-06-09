package com.bookworm.domain.vo.user;

import com.bookworm.domain.exception.InvalidEmailException;
import com.bookworm.domain.validation.utils.EmailValidationUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Email {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String value;

    private Email(String value) {
        validate(value);
        this.value = value.toLowerCase().trim();
    }

    /**
     * 정적 팩토리 메서드
     */
    public static Email of(String value) {
        return new Email(value);
    }

    private void validate(String email) {
        try {
            EmailValidationUtils.validateEmailWithException(email);
        } catch (IllegalArgumentException e) {
            throw new InvalidEmailException(e.getMessage());
        }
    }

    /**
     * 마스킹된 이메일 반환
     * 예: john.doe@example.com → j***@example.com
     */
    public String getMasked() {
        if (value == null || !value.contains("@")) {
            return value;
        }

        String[] parts = value.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***@" + domain;
        } else {
            return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + domain;
        }
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return value.equals(email.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}