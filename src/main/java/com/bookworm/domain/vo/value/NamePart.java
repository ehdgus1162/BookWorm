package com.bookworm.domain.vo.value;

import com.bookworm.domain.vo.exception.NameException;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

/**
 * 이름 부분(FirstName, LastName)의 공통 로직을 담당하는 추상 클래스
 */
@Embeddable
@Getter
public abstract class NamePart {

    private final String value;

    /**
     * JPA를 위한 기본 생성자
     */
    protected NamePart() {
        this.value = null;
    }

    /**
     * 값을 받아 유효성 검증 후 객체 생성
     *
     * @param value 검증할 값
     */
    protected NamePart(String value) {
        validate(value);
        this.value = value.trim();
    }

    /**
     * 템플릿 메서드 - 모든 검증을 순차적으로 실행
     * @param value 검증할 값
     */
    protected final void validate(String value) {
        validateNotEmpty(value);
        validateLength(value);
    }

    /**
     * 빈 값 검증 - 모든 이름 부분에 공통으로 적용
     * @param value 검증할 값
     */
    protected void validateNotEmpty(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new NameException.EmptyNameException();
        }
    }

    /**
     * 길이 검증 - 하위 클래스에서 구체적인 구현 제공
     * @param value 검증할 값
     */
    protected abstract void validateLength(String value);


    /**
     * 추가 규칙 검증 - 하위 클래스에서 필요시 오버라이드
     * 기본 구현은 아무 검증도 하지 않음
     * @param value 검증할 값
     */
    protected void validateAdditionalRules(String value) {
        // 기본 구현은 비어있음 - 하위 클래스에서 필요시 오버라이드
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamePart namePart = (NamePart) o;
        return Objects.equals(value, namePart.value);
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
