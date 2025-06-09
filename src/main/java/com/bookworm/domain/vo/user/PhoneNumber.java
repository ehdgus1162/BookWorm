package com.bookworm.domain.vo.user;

import com.bookworm.domain.validation.utils.PhoneNumberValidationUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * 전화번호 값 객체
 * 전화번호의 유효성과 불변성을 보장합니다.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class PhoneNumber {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{3}-\\d{3,4}-\\d{4}$");

    @Column(name = "phone_number")
    private String value;

    /**
     * 전화번호 값 객체 생성
     * 생성 시 유효성을 검증하여 항상 유효한 상태를 유지합니다.
     *
     * @param value 전화번호
     * @throws IllegalArgumentException 유효하지 않은 전화번호인 경우
     */
    public PhoneNumber(String value) {
        validate(value);
        this.value = value;
    }

    /**
     * 전화번호 유효성 검증
     */
    private void validate(String phoneNumber) {
        PhoneNumberValidationUtils.validatePhoneNumberWithException(phoneNumber);
    }

    /**
     * 전화번호를 문자열로 반환
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * 전화번호가 같은지 비교
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return value.equals(that.value);
    }

    /**
     * 해시코드 계산
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }
}