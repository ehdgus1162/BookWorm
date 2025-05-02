package com.bookworm.domain.vo.value;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
public class FullName implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final LastName lastName;
    private final FirstName firstName;

    /**
     * JPA 위한 기본 생성자
     */
    protected FullName() {
        this.lastName = null;
        this.firstName = null;
    }

    /**
     * 성과 이름을 받아 객체 생성
     *
     * @param lastName 성
     * @param firstName 이름
     */
    public FullName(LastName lastName, FirstName firstName) {
        this.lastName = lastName;
        this.firstName = firstName;
    }

    /**
     * 문자열 값으로부터 전체 이름 객체 생성
     *
     * @param lastName 성 문자열
     * @param firstName 이름 문자열
     * @return 전체 이름 객체
     */
    public static FullName of(String lastName, String firstName) {
        return new FullName(
                new LastName(lastName),
                new FirstName(firstName)
        );
    }

    /**
     * 전체 이름 문자열 반환 (성 + 이름)
     *
     * @return 전체 이름
     */
    public String getFullName() {
        return lastName.getValue() + firstName.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullName fullName = (FullName) o;
        return Objects.equals(lastName, fullName.lastName) &&
                Objects.equals(firstName, fullName.firstName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastName, firstName);
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
