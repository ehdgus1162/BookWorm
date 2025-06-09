package com.bookworm.domain.vo.user;

import com.bookworm.domain.validation.utils.AddressValidationUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주소 값 객체
 * 주소의 유효성과 불변성을 보장합니다.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class Address {
    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    /**
     * 주소 값 객체 생성
     * 생성 시 유효성을 검증하여 항상 유효한 상태를 유지합니다.
     */
    public Address(String street, String city, String state, String country) {
        validate(street, city, state, country);
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
    }



    /**
     * 주소 유효성 검증
     */
    private void validate(String street, String city, String state, String country) {
        AddressValidationUtils.validateAddressWithException(street, city, state, country);
    }

    /**
     * 주소를 문자열로 반환
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(street);

        if (city != null && !city.isBlank()) {
            sb.append(", ").append(city);
        }

        if (state != null && !state.isBlank()) {
            sb.append(", ").append(state);
        }

        if (country != null && !country.isBlank()) {
            sb.append(", ").append(country);
        }

        return sb.toString();
    }

    /**
     * 주소가 같은지 비교
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return equals(street, address.street) &&
                equals(city, address.city) &&
                equals(state, address.state) &&
                equals(country, address.country);
    }

    private boolean equals(String a, String b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    /**
     * 해시코드 계산
     */
    @Override
    public int hashCode() {
        int result = street != null ? street.hashCode() : 0;
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        return result;
    }
}