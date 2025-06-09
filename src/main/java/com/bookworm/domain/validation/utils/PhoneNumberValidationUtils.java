package com.bookworm.domain.validation.utils;

import com.bookworm.domain.constant.ErrorMessages;
import com.bookworm.domain.exception.PhoneNumberValidationException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 전화번호 검증 로직을 캡슐화한 유틸리티 클래스
 *
 * <p>이 클래스는 다양한 전화번호 형식을 지원하며, Bean Validation과 도메인 객체
 * 모두에서 사용할 수 있도록 설계되었습니다.</p>
 *
 * <p>지원하는 형식:</p>
 * <ul>
 *   <li>휴대폰: 010-1234-5678, 011-123-4567</li>
 *   <li>일반전화: 02-123-4567, 031-1234-5678</li>
 * </ul>
 */
@Slf4j
public final class PhoneNumberValidationUtils {

    // 컴파일된 정규표현식 패턴들 (성능 최적화)
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{2,3}-\\d{3,4}-\\d{4}$");

    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("^01[0-9]-\\d{3,4}-\\d{4}$");

    private static final Pattern LANDLINE_PATTERN =
            Pattern.compile("^(02|0[3-9]\\d)-\\d{3,4}-\\d{4}$");

    private static final Pattern DIGITS_ONLY_PATTERN =
            Pattern.compile("^\\d{10,11}$");

    private static final Pattern SPECIAL_CHARS_PATTERN =
            Pattern.compile("[^0-9-]");

    // 상수들
    private static final int MIN_PHONE_LENGTH = 10;
    private static final int MAX_PHONE_LENGTH = 13; // 하이픈 포함
    private static final int MOBILE_DIGITS_LENGTH = 11;
    private static final int LANDLINE_DIGITS_LENGTH_MIN = 9;
    private static final int LANDLINE_DIGITS_LENGTH_MAX = 10;

    private PhoneNumberValidationUtils() {
        // 유틸리티 클래스이므로 인스턴스화 방지
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 전화번호 유효성 검증 (상세한 검증)
     *
     * @param phoneNumber 검증할 전화번호
     * @return 오류 메시지 목록 (비어있으면 유효한 전화번호)
     */
    public static List<String> validatePhoneNumber(String phoneNumber) {
        List<String> errors = new ArrayList<>();

        // 1. null 체크
        if (phoneNumber == null) {
            errors.add(ErrorMessages.PhoneNumber.REQUIRED);
            return errors;
        }

        // 2. 공백 체크
        String trimmed = phoneNumber.trim();
        if (trimmed.isEmpty()) {
            errors.add(ErrorMessages.PhoneNumber.EMPTY);
            return errors;
        }

        // 3. 길이 체크 (기본적인 범위)
        if (trimmed.length() < MIN_PHONE_LENGTH || trimmed.length() > MAX_PHONE_LENGTH) {
            errors.add(ErrorMessages.PhoneNumber.INVALID_LENGTH);
        }

        // 4. 허용되지 않는 특수문자 체크
        if (SPECIAL_CHARS_PATTERN.matcher(trimmed).find()) {
            errors.add(ErrorMessages.PhoneNumber.INVALID_DIGITS);
        }

        // 5. 기본 형식 체크
        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            errors.add(ErrorMessages.PhoneNumber.INVALID_FORMAT);
            return errors; // 기본 형식이 틀리면 추가 검증 불필요
        }

        // 6. 세부 형식 검증
        validateDetailedFormat(trimmed, errors);

        return errors;
    }

    /**
     * 세부적인 전화번호 형식 검증
     */
    private static void validateDetailedFormat(String phoneNumber, List<String> errors) {
        String digitsOnly = phoneNumber.replaceAll("-", "");

        // 휴대폰 번호 검증
        if (phoneNumber.startsWith("01")) {
            if (digitsOnly.length() != MOBILE_DIGITS_LENGTH) {
                errors.add("휴대폰 번호는 11자리여야 합니다.");
                return;
            }

            if (!MOBILE_PATTERN.matcher(phoneNumber).matches()) {
                errors.add("올바른 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)");
            }
        }
        // 일반전화 번호 검증
        else if (phoneNumber.startsWith("02") || phoneNumber.matches("^0[3-9].*")) {
            int digitsLength = digitsOnly.length();
            if (digitsLength < LANDLINE_DIGITS_LENGTH_MIN || digitsLength > LANDLINE_DIGITS_LENGTH_MAX) {
                errors.add("일반전화 번호는 9-10자리여야 합니다.");
                return;
            }

            if (!LANDLINE_PATTERN.matcher(phoneNumber).matches()) {
                errors.add("올바른 일반전화 번호 형식이 아닙니다. (예: 02-123-4567)");
            }
        }
        else {
            errors.add("지원하지 않는 전화번호 형식입니다.");
        }
    }

    /**
     * 전화번호 정규화 (하이픈 추가)
     *
     * @param phoneNumber 정규화할 전화번호 (숫자만 또는 하이픈 포함)
     * @return 정규화된 전화번호 (형식: XXX-XXXX-XXXX)
     * @throws PhoneNumberValidationException 정규화할 수 없는 형식인 경우
     */
    public static String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new PhoneNumberValidationException(ErrorMessages.PhoneNumber.REQUIRED);
        }

        // 모든 특수문자 제거 (숫자만 추출)
        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");

        // 숫자만으로 구성된 유효한 길이인지 확인
        if (!DIGITS_ONLY_PATTERN.matcher(digitsOnly).matches()) {
            throw new PhoneNumberValidationException(
                    "전화번호는 10-11자리 숫자여야 합니다. 입력값: " + phoneNumber
            );
        }

        try {
            return formatPhoneNumber(digitsOnly);
        } catch (Exception e) {
            log.error("전화번호 정규화 실패: {}", phoneNumber, e);
            throw new PhoneNumberValidationException(
                    "전화번호 정규화에 실패했습니다: " + phoneNumber, e
            );
        }
    }

    /**
     * 숫자 문자열을 전화번호 형식으로 포맷
     */
    private static String formatPhoneNumber(String digitsOnly) {
        int length = digitsOnly.length();

        if (length == MOBILE_DIGITS_LENGTH) {
            // 11자리 (휴대폰): 010-1234-5678
            return digitsOnly.substring(0, 3) + "-" +
                    digitsOnly.substring(3, 7) + "-" +
                    digitsOnly.substring(7);
        } else if (length == 10) {
            // 10자리: 지역번호에 따라 분할
            if (digitsOnly.startsWith("02")) {
                // 서울 (02): 02-1234-5678
                return digitsOnly.substring(0, 2) + "-" +
                        digitsOnly.substring(2, 6) + "-" +
                        digitsOnly.substring(6);
            } else {
                // 기타 지역 (031, 032 등): 031-123-4567
                return digitsOnly.substring(0, 3) + "-" +
                        digitsOnly.substring(3, 6) + "-" +
                        digitsOnly.substring(6);
            }
        } else if (length == 9) {
            // 9자리 (일부 지역번호): 031-123-4567
            return digitsOnly.substring(0, 3) + "-" +
                    digitsOnly.substring(3, 6) + "-" +
                    digitsOnly.substring(6);
        }

        throw new PhoneNumberValidationException(
                "지원하지 않는 전화번호 길이입니다: " + length + "자리"
        );
    }

    /**
     * 전화번호가 유효한지 확인
     *
     * @param phoneNumber 검증할 전화번호
     * @return 유효 여부
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        List<String> errors = validatePhoneNumber(phoneNumber);
        boolean isValid = errors.isEmpty();

        if (!isValid) {
            log.debug("전화번호 검증 실패: {} - 오류: {}", phoneNumber, errors);
        }

        return isValid;
    }

    /**
     * 전화번호 유효성 검증 (예외 발생)
     *
     * @param phoneNumber 검증할 전화번호
     * @throws PhoneNumberValidationException 유효하지 않은 전화번호인 경우
     */
    public static void validatePhoneNumberWithException(String phoneNumber) {
        List<String> errors = validatePhoneNumber(phoneNumber);

        if (!errors.isEmpty()) {
            String errorMessage = String.join(" ", errors);
            log.warn("전화번호 검증 실패: {} - {}", phoneNumber, errorMessage);
            throw new PhoneNumberValidationException(errorMessage);
        }
    }

    /**
     * 휴대폰 번호인지 확인
     *
     * @param phoneNumber 확인할 전화번호
     * @return 휴대폰 번호 여부
     */
    public static boolean isMobileNumber(String phoneNumber) {
        if (!isValidPhoneNumber(phoneNumber)) {
            return false;
        }

        return MOBILE_PATTERN.matcher(phoneNumber).matches();
    }

    /**
     * 일반전화 번호인지 확인
     *
     * @param phoneNumber 확인할 전화번호
     * @return 일반전화 번호 여부
     */
    public static boolean isLandlineNumber(String phoneNumber) {
        if (!isValidPhoneNumber(phoneNumber)) {
            return false;
        }

        return LANDLINE_PATTERN.matcher(phoneNumber).matches();
    }

    /**
     * 전화번호에서 숫자만 추출
     *
     * @param phoneNumber 전화번호
     * @return 숫자만 포함된 문자열
     */
    public static String extractDigitsOnly(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }
        return phoneNumber.replaceAll("[^0-9]", "");
    }
}