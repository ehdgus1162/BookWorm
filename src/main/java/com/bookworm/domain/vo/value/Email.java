package com.bookworm.domain.vo.value;

import com.bookworm.domain.vo.validator.EmailValidator;
import com.bookworm.domain.vo.validator.EmailValidatorImpl;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 이메일 값 객체
 * 이메일 형식 유효성 검증 및 이메일 주소 캡슐화
 */
@Embeddable
@Getter
public class Email implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String value;
    private static final EmailValidator validator = new EmailValidatorImpl() {
    };

    /**
     * 이메일 값을 받아 유효성 검증 후 객체 생성
     *
     * @param value 이메일 문자열
     */
    public Email(String value) {
        this.value = value;
    }

    public Email() {
    }

    /**
     * 이메일 객체 생성 팩토리 메서드
     */
    public static Email of(String value) {
        validator.validate(value);
        return new Email(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}