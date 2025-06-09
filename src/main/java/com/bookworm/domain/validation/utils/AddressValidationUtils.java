package com.bookworm.domain.validation.utils;

import com.bookworm.domain.constant.ErrorMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * 주소 검증 로직을 캡슐화한 유틸리티 클래스
 * Bean Validation과 도메인 객체 모두에서 사용할 수 있습니다.
 */
public class AddressValidationUtils {

    /**
     * 주소 필드 유효성 검증
     *
     * @param street 도로명 주소
     * @param city 도시명
     * @param state 주/도
     * @param country 국가
     * @return 오류 메시지 목록 (비어있으면 유효한 주소)
     */
    public static List<String> validateAddress(String street, String city, String state, String country) {
        List<String> errors = new ArrayList<>();

        // 필수 필드 검증
        if (street == null || street.isBlank()) {
            errors.add(ErrorMessages.Address.STREET_REQUIRED);
        }

        if (city == null || city.isBlank()) {
            errors.add(ErrorMessages.Address.CITY_REQUIRED);
        }

        if (country == null || country.isBlank()) {
            errors.add(ErrorMessages.Address.COUNTRY_REQUIRED);
        }

        // 길이 검증 (필수 필드가 null이 아닌 경우에만 검증)
        if (street != null && street.length() > 100) {
            errors.add(ErrorMessages.Address.STREET_TOO_LONG);
        }

        if (city != null && city.length() > 50) {
            errors.add(ErrorMessages.Address.CITY_TOO_LONG);
        }

        if (state != null && state.length() > 50) {
            errors.add(ErrorMessages.Address.STATE_TOO_LONG);
        }

        if (country != null && country.length() > 50) {
            errors.add(ErrorMessages.Address.COUNTRY_TOO_LONG);
        }

        return errors;
    }

    /**
     * 주소가 유효한지 확인
     */
    public static boolean isValidAddress(String street, String city, String state, String country) {
        return validateAddress(street, city, state, country).isEmpty();
    }

    /**
     * 주소 유효성 검증 (예외 발생)
     *
     * @throws IllegalArgumentException 유효하지 않은 주소인 경우
     */
    public static void validateAddressWithException(String street, String city, String state, String country) {
        List<String> errors = validateAddress(street, city, state, country);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }
    }
}