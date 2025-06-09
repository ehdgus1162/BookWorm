package com.bookworm.domain.vo.user;

import com.bookworm.domain.validation.utils.NameValidationUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 성(Last Name) 값 객체
 * 성의 유효성과 불변성을 보장합니다.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class LastName {
    @Column(name = "last_name")
    private String value;

    /**
     * 성 값 객체 생성
     * 생성 시 유효성을 검증하여 항상 유효한 상태를 유지합니다.
     *
     * @param value 성
     * @throws IllegalArgumentException 유효하지 않은 성인 경우
     */
    public LastName(String value) {
        validate(value);
        this.value = value;
    }

    /**
     * 성 유효성 검증
     */
    private void validate(String name) {
        NameValidationUtils.validateNameWithException(name, false);
    }

    /**
     * 성을 문자열로 반환
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * 성이 같은지 비교
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LastName lastName = (LastName) o;
        return value.equals(lastName.value);
    }

    /**
     * 해시코드 계산
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }
}