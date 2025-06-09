package com.bookworm.domain.validation.utils;

import com.bookworm.domain.constant.ErrorMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 이름 관련 검증 로직을 캡슐화한 유틸리티 클래스
 * Bean Validation과 도메인 객체 모두에서 사용할 수 있습니다.
 */
public class NameValidationUtils {

    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z가-힣\\s]+");

    /**
     * 이름 유효성 검증
     *
     * @param name 검증할 이름
     * @param isFirstName 이름(true) 또는 성(false) 여부
     * @return 오류 메시지 목록 (비어있으면 유효한 이름)
     */
    public static List<String> validateName(String name, boolean isFirstName) {
        List<String> errors = new ArrayList<>();

        // 필수 값 검증
        if (name == null || name.isBlank()) {
            errors.add(isFirstName ?
                    ErrorMessages.FirstName.REQUIRED :
                    ErrorMessages.LastName.REQUIRED);
            return errors; // 이름이 없으면 추가 검증 불필요
        }

        // 길이 검증
        if (name.length() > 50) {
            errors.add(isFirstName ?
                    ErrorMessages.FirstName.TOO_LONG :
                    ErrorMessages.LastName.TOO_LONG);
        }

        // 문자 검증 (영문자, 한글, 공백만 허용)
        if (!NAME_PATTERN.matcher(name).matches()) {
            errors.add(isFirstName ?
                    ErrorMessages.FirstName.INVALID_CHARS :
                    ErrorMessages.LastName.INVALID_CHARS);
        }

        return errors;
    }

    /**
     * 이름 유효성 검증 (예외 발생)
     *
     * @param name 검증할 이름
     * @param isFirstName 이름(true) 또는 성(false) 여부
     * @throws IllegalArgumentException 유효하지 않은 이름인 경우
     */
    public static void validateNameWithException(String name, boolean isFirstName) {
        List<String> errors = validateName(name, isFirstName);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }
    }
}