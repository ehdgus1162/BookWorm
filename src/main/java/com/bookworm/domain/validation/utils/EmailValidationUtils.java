package com.bookworm.domain.validation.utils;

import com.bookworm.domain.constant.ErrorMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 이메일 검증 로직을 캡슐화한 유틸리티 클래스
 * Bean Validation과 도메인 객체 모두에서 사용할 수 있습니다.
 */
public class EmailValidationUtils {

    // 이메일 정규식 패턴
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    /**
     * 이메일 유효성 검증
     * @param email 검증할 이메일
     * @return 오류 메시지 목록 (비어있으면 유효한 이메일)
     */
    public static List<String> validateEmail(String email) {
        List<String> errors = new ArrayList<>();

        // 필수 값 검증
        if (email == null || email.isBlank()) {
            errors.add(ErrorMessages.Email.REQUIRED);
            return errors; // 이메일이 없으면 추가 검증 불필요
        }

        // 형식 검증
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add(ErrorMessages.Email.INVALID_FORMAT);
        }

        // 길이 검증
        if (email.length() > 100) {
            errors.add(ErrorMessages.Email.TOO_LONG);
        }

        return errors;
    }

    /**
     * 이메일이 유효한지 확인
     * @param email 검증할 이메일
     * @return 유효 여부
     */
    public static boolean isValidEmail(String email) {
        return validateEmail(email).isEmpty();
    }

    /**
     * 이메일 유효성 검증 (예외 발생)
     * @param email 검증할 이메일
     * @throws IllegalArgumentException 유효하지 않은 이메일인 경우
     */
    public static void validateEmailWithException(String email) {
        List<String> errors = validateEmail(email);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }
    }
}