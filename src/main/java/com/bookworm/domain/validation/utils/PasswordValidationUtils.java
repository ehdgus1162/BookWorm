package com.bookworm.domain.validation.utils;

import com.bookworm.domain.constant.ErrorMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 비밀번호 검증 로직을 캡슐화한 유틸리티 클래스
 * 이 클래스는 Bean Validation과 도메인 객체 모두에서 사용할 수 있습니다.
 */
public class PasswordValidationUtils {

    private static final Pattern SPECIAL_CHAR_PATTERN =
            Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    /**
     * 비밀번호 유효성 검증
     * @param password 검증할 비밀번호
     * @return 오류 메시지 목록 (비어있으면 유효한 비밀번호)
     */
    public static List<String> validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        // 필수 값 검증
        if (password == null || password.isBlank()) {
            errors.add(ErrorMessages.Password.REQUIRED);
            return errors; // 비밀번호가 없으면 추가 검증 불필요
        }

        // 길이 검증
        if (password.length() < 8) {
            errors.add(ErrorMessages.Password.TOO_SHORT);
        }

        if (password.length() > 100) {
            errors.add(ErrorMessages.Password.TOO_LONG);
        }

        // 복잡성 검증
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = SPECIAL_CHAR_PATTERN.matcher(password).find();

        if (!hasUppercase) {
            errors.add(ErrorMessages.Password.MISSING_UPPERCASE);
        }

        if (!hasLowercase) {
            errors.add(ErrorMessages.Password.MISSING_LOWERCASE);
        }

        if (!hasDigit) {
            errors.add(ErrorMessages.Password.MISSING_DIGIT);
        }

        if (!hasSpecialChar) {
            errors.add(ErrorMessages.Password.MISSING_SPECIAL_CHAR);
        }

        return errors;
    }

    /**
     * 비밀번호가 유효한지 확인
     * @param password 검증할 비밀번호
     * @return 유효 여부
     */
    public static boolean isValidPassword(String password) {
        return validatePassword(password).isEmpty();
    }

    /**
     * 비밀번호 유효성 검증 (예외 발생)
     * @param password 검증할 비밀번호
     * @throws IllegalArgumentException 유효하지 않은 비밀번호인 경우
     */
    public static void validatePasswordWithException(String password) {
        List<String> errors = validatePassword(password);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }
    }
}