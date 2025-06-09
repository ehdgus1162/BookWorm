package com.bookworm.domain.constant;

/**
 * 도메인 모델의 유효성 검증에 사용되는 에러 메시지 상수들을 정의합니다.
 * 모든 Value Object(VO)에서 공통으로 사용할 수 있도록 중앙화되어 있습니다.
 */
public class ErrorMessages {

    // 공통 메시지
    public static final String REQUIRED_VALUE = "필수 입력값입니다";

    // Bean Validation 공통 메시지 (메시지 프로퍼티 파일 참조용)
    public static final class ValidationMessages {
        public static final String PREFIX = "{";
        public static final String SUFFIX = "}";

        // 메시지 키를 Bean Validation 형식으로 변환하는 유틸리티 메소드
        public static String toMessageTemplate(String messageKey) {
            return PREFIX + messageKey + SUFFIX;
        }
    }

    // Email 관련 메시지
    public static final class Email {
        public static final String REQUIRED = "이메일은 " + REQUIRED_VALUE;
        public static final String INVALID_FORMAT = "유효하지 않은 이메일 형식입니다";
        public static final String TOO_LONG = "이메일은 100자를 초과할 수 없습니다";

        // Bean Validation 메시지 키
        public static final String KEY_REQUIRED = "email.required";
        public static final String KEY_INVALID_FORMAT = "email.invalid.format";
        public static final String KEY_TOO_LONG = "email.too.long";

        // Bean Validation 템플릿
        public static final String TEMPLATE_REQUIRED = ValidationMessages.toMessageTemplate(KEY_REQUIRED);
        public static final String TEMPLATE_INVALID_FORMAT = ValidationMessages.toMessageTemplate(KEY_INVALID_FORMAT);
        public static final String TEMPLATE_TOO_LONG = ValidationMessages.toMessageTemplate(KEY_TOO_LONG);
    }

    // Password 관련 메시지
    public static final class Password {

        public static final String TOO_SHORT_TEMPLATE = "비밀번호는 최소 %d자 이상이어야 합니다.";
        public static final String TOO_LONG_TEMPLATE = "비밀번호는 최대 %d자를 초과할 수 없습니다.";
        public static final String REQUIRED = "비밀번호는 " + REQUIRED_VALUE;
        public static final String TOO_SHORT = "비밀번호는 최소 8자 이상이어야 합니다";
        public static final String MISSING_UPPERCASE = "비밀번호에는 대문자가 포함되어야 합니다";
        public static final String MISSING_LOWERCASE = "비밀번호에는 소문자가 포함되어야 합니다";
        public static final String MISSING_DIGIT = "비밀번호에는 숫자가 포함되어야 합니다";
        public static final String MISSING_SPECIAL_CHAR = "비밀번호에는 특수문자가 포함되어야 합니다";
        public static final String INVALID_FORMAT = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다";
        public static final String TOO_LONG = "비밀번호는 100자를 초과할 수 없습니다";

        // Bean Validation 메시지 키
        public static final String KEY_REQUIRED = "password.required";
        public static final String KEY_TOO_SHORT = "password.too.short";
        public static final String KEY_TOO_LONG = "password.too.long";
        public static final String KEY_MISSING_UPPERCASE = "password.uppercase.required";
        public static final String KEY_MISSING_LOWERCASE = "password.lowercase.required";
        public static final String KEY_MISSING_DIGIT = "password.digit.required";
        public static final String KEY_MISSING_SPECIAL_CHAR = "password.special.char.required";
        public static final String KEY_INVALID_FORMAT = "password.invalid.format";
        public static final String KEY_VALIDATION_SUMMARY = "password.validation.summary";

        // Bean Validation 템플릿
        public static final String TEMPLATE_REQUIRED = ValidationMessages.toMessageTemplate(KEY_REQUIRED);
        public static final String TEMPLATE_TOO_SHORT = ValidationMessages.toMessageTemplate(KEY_TOO_SHORT);
        public static final String TEMPLATE_TOO_LONG = ValidationMessages.toMessageTemplate(KEY_TOO_LONG);
        public static final String TEMPLATE_MISSING_UPPERCASE = ValidationMessages.toMessageTemplate(KEY_MISSING_UPPERCASE);
        public static final String TEMPLATE_MISSING_LOWERCASE = ValidationMessages.toMessageTemplate(KEY_MISSING_LOWERCASE);
        public static final String TEMPLATE_MISSING_DIGIT = ValidationMessages.toMessageTemplate(KEY_MISSING_DIGIT);
        public static final String TEMPLATE_MISSING_SPECIAL_CHAR = ValidationMessages.toMessageTemplate(KEY_MISSING_SPECIAL_CHAR);
        public static final String TEMPLATE_INVALID_FORMAT = ValidationMessages.toMessageTemplate(KEY_INVALID_FORMAT);
        public static final String TEMPLATE_VALIDATION_SUMMARY = ValidationMessages.toMessageTemplate(KEY_VALIDATION_SUMMARY);
    }

    // FirstName 관련 메시지
    public static final class FirstName {
        public static final String REQUIRED = "이름은 " + REQUIRED_VALUE;
        public static final String TOO_LONG = "이름은 50자를 초과할 수 없습니다";
        public static final String INVALID_CHARS = "이름에는 영문자, 한글, 공백만 포함할 수 있습니다";

        // Bean Validation 메시지 키
        public static final String KEY_REQUIRED = "firstname.required";
        public static final String KEY_TOO_LONG = "firstname.too.long";
        public static final String KEY_INVALID_CHARS = "firstname.invalid.chars";

        // Bean Validation 템플릿
        public static final String TEMPLATE_REQUIRED = ValidationMessages.toMessageTemplate(KEY_REQUIRED);
        public static final String TEMPLATE_TOO_LONG = ValidationMessages.toMessageTemplate(KEY_TOO_LONG);
        public static final String TEMPLATE_INVALID_CHARS = ValidationMessages.toMessageTemplate(KEY_INVALID_CHARS);
    }

    // LastName 관련 메시지
    public static final class LastName {
        public static final String REQUIRED = "성은 " + REQUIRED_VALUE;
        public static final String TOO_LONG = "성은 50자를 초과할 수 없습니다";
        public static final String INVALID_CHARS = "성에는 영문자, 한글, 공백만 포함할 수 있습니다";

        // Bean Validation 메시지 키
        public static final String KEY_REQUIRED = "lastname.required";
        public static final String KEY_TOO_LONG = "lastname.too.long";
        public static final String KEY_INVALID_CHARS = "lastname.invalid.chars";

        // Bean Validation 템플릿
        public static final String TEMPLATE_REQUIRED = ValidationMessages.toMessageTemplate(KEY_REQUIRED);
        public static final String TEMPLATE_TOO_LONG = ValidationMessages.toMessageTemplate(KEY_TOO_LONG);
        public static final String TEMPLATE_INVALID_CHARS = ValidationMessages.toMessageTemplate(KEY_INVALID_CHARS);
    }

    // PhoneNumber 관련 메시지
    public static final class PhoneNumber {
        public static final String REQUIRED = "전화번호는 " + REQUIRED_VALUE;
        public static final String INVALID_FORMAT = "유효하지 않은 전화번호 형식입니다. 형식: 000-0000-0000";

        // Bean Validation 메시지 키
        public static final String KEY_REQUIRED = "phone.required";
        public static final String KEY_INVALID_FORMAT = "phone.invalid.format";

        private PhoneNumber() {}

        public static final String EMPTY = "전화번호는 빈 값일 수 없습니다.";
        public static final String INVALID_LENGTH = "전화번호 길이가 올바르지 않습니다.";
        public static final String INVALID_DIGITS = "전화번호는 숫자와 하이픈만 포함할 수 있습니다.";

        // Bean Validation 템플릿
        public static final String TEMPLATE_REQUIRED = ValidationMessages.toMessageTemplate(KEY_REQUIRED);
        public static final String TEMPLATE_INVALID_FORMAT = ValidationMessages.toMessageTemplate(KEY_INVALID_FORMAT);
    }

    // Address 관련 메시지
    public static final class Address {
        public static final String STREET_REQUIRED = "도로명 주소는 " + REQUIRED_VALUE;
        public static final String CITY_REQUIRED = "도시명은 " + REQUIRED_VALUE;
        public static final String COUNTRY_REQUIRED = "국가명은 " + REQUIRED_VALUE;

        public static final String STREET_TOO_LONG = "도로명 주소는 100자를 초과할 수 없습니다";
        public static final String CITY_TOO_LONG = "도시명은 50자를 초과할 수 없습니다";
        public static final String STATE_TOO_LONG = "주/도는 50자를 초과할 수 없습니다";
        public static final String COUNTRY_TOO_LONG = "국가명은 50자를 초과할 수 없습니다";

        // Bean Validation 메시지 키
        public static final String KEY_STREET_REQUIRED = "address.street.required";
        public static final String KEY_CITY_REQUIRED = "address.city.required";
        public static final String KEY_COUNTRY_REQUIRED = "address.country.required";
        public static final String KEY_STREET_TOO_LONG = "address.street.too.long";
        public static final String KEY_CITY_TOO_LONG = "address.city.too.long";
        public static final String KEY_STATE_TOO_LONG = "address.state.too.long";
        public static final String KEY_COUNTRY_TOO_LONG = "address.country.too.long";

        // Bean Validation 템플릿
        public static final String TEMPLATE_STREET_REQUIRED = ValidationMessages.toMessageTemplate(KEY_STREET_REQUIRED);
        public static final String TEMPLATE_CITY_REQUIRED = ValidationMessages.toMessageTemplate(KEY_CITY_REQUIRED);
        public static final String TEMPLATE_COUNTRY_REQUIRED = ValidationMessages.toMessageTemplate(KEY_COUNTRY_REQUIRED);
        public static final String TEMPLATE_STREET_TOO_LONG = ValidationMessages.toMessageTemplate(KEY_STREET_TOO_LONG);
        public static final String TEMPLATE_CITY_TOO_LONG = ValidationMessages.toMessageTemplate(KEY_CITY_TOO_LONG);
        public static final String TEMPLATE_STATE_TOO_LONG = ValidationMessages.toMessageTemplate(KEY_STATE_TOO_LONG);
        public static final String TEMPLATE_COUNTRY_TOO_LONG = ValidationMessages.toMessageTemplate(KEY_COUNTRY_TOO_LONG);
    }

    // 사용자 상태 관련 메시지
    public static final class UserStatus {
        public static final String INVALID = "유효하지 않은 사용자 상태입니다";

        // Bean Validation 메시지 키
        public static final String KEY_INVALID = "user.status.invalid";

        // Bean Validation 템플릿
        public static final String TEMPLATE_INVALID = ValidationMessages.toMessageTemplate(KEY_INVALID);
    }

    // 권한 관련 메시지
    public static final class Role {
        public static final String INVALID = "유효하지 않은 사용자 권한입니다";

        // Bean Validation 메시지 키
        public static final String KEY_INVALID = "role.invalid";

        // Bean Validation 템플릿
        public static final String TEMPLATE_INVALID = ValidationMessages.toMessageTemplate(KEY_INVALID);
    }

    // 중복 검사 관련 메시지
    public static final class Duplication {
        public static final String EMAIL_EXISTS = "이미 등록된 이메일입니다";
        public static final String PHONE_EXISTS = "이미 등록된 전화번호입니다";

        // Bean Validation 메시지 키
        public static final String KEY_EMAIL_EXISTS = "duplication.email.exists";
        public static final String KEY_PHONE_EXISTS = "duplication.phone.exists";

        // Bean Validation 템플릿
        public static final String TEMPLATE_EMAIL_EXISTS = ValidationMessages.toMessageTemplate(KEY_EMAIL_EXISTS);
        public static final String TEMPLATE_PHONE_EXISTS = ValidationMessages.toMessageTemplate(KEY_PHONE_EXISTS);
    }

    // 기타 유효성 검증 메시지
    public static final class Validation {
        public static final String PASSWORDS_NOT_MATCH = "비밀번호와 비밀번호 확인이 일치하지 않습니다";
        public static final String INVALID_DATE_FORMAT = "유효하지 않은 날짜 형식입니다";

        // Bean Validation 메시지 키
        public static final String KEY_PASSWORDS_NOT_MATCH = "validation.passwords.not.match";
        public static final String KEY_INVALID_DATE_FORMAT = "validation.invalid.date.format";

        // Bean Validation 템플릿
        public static final String TEMPLATE_PASSWORDS_NOT_MATCH = ValidationMessages.toMessageTemplate(KEY_PASSWORDS_NOT_MATCH);
        public static final String TEMPLATE_INVALID_DATE_FORMAT = ValidationMessages.toMessageTemplate(KEY_INVALID_DATE_FORMAT);
    }

    // 커스텀 어노테이션 관련 메시지
    public static final class CustomAnnotation {
        // ValidEmail 어노테이션 메시지
        public static final String EMAIL_DEFAULT = "유효하지 않은 이메일 형식입니다";
        public static final String KEY_EMAIL_DEFAULT = "custom.valid.email";
        public static final String TEMPLATE_EMAIL_DEFAULT = ValidationMessages.toMessageTemplate(KEY_EMAIL_DEFAULT);

        // ValidPassword 어노테이션 메시지
        public static final String PASSWORD_DEFAULT = "비밀번호는 8자 이상이며, 대문자, 소문자, 숫자, 특수 문자를 포함해야 합니다";
        public static final String KEY_PASSWORD_DEFAULT = "custom.valid.password";
        public static final String TEMPLATE_PASSWORD_DEFAULT = ValidationMessages.toMessageTemplate(KEY_PASSWORD_DEFAULT);

        // ValidPhoneNumber 어노테이션 메시지
        public static final String PHONE_DEFAULT = "유효하지 않은 전화번호 형식입니다 (예: 010-1234-5678)";
        public static final String KEY_PHONE_DEFAULT = "custom.valid.phone";
        public static final String TEMPLATE_PHONE_DEFAULT = ValidationMessages.toMessageTemplate(KEY_PHONE_DEFAULT);

        // ValidName 어노테이션 메시지
        public static final String NAME_DEFAULT = "이름은 영문자, 한글, 공백만 포함할 수 있으며 50자를 초과할 수 없습니다";
        public static final String KEY_NAME_DEFAULT = "custom.valid.name";
        public static final String TEMPLATE_NAME_DEFAULT = ValidationMessages.toMessageTemplate(KEY_NAME_DEFAULT);
    }

    // 유효성 검증 그룹 관련 메시지
    public static final class ValidationGroups {
        // 생성 시 검증 메시지
        public static final String CREATE_PREFIX = "생성 시 ";
        public static final String KEY_CREATE_PREFIX = "validation.group.create";
        public static final String TEMPLATE_CREATE_PREFIX = ValidationMessages.toMessageTemplate(KEY_CREATE_PREFIX);

        // 수정 시 검증 메시지
        public static final String UPDATE_PREFIX = "수정 시 ";
        public static final String KEY_UPDATE_PREFIX = "validation.group.update";
        public static final String TEMPLATE_UPDATE_PREFIX = ValidationMessages.toMessageTemplate(KEY_UPDATE_PREFIX);
    }

    // 공통 파라미터화된 메시지
    public static final class ParameterizedMessages {
        public static final String MIN_LENGTH = "최소 {min}자 이상이어야 합니다";
        public static final String MAX_LENGTH = "최대 {max}자까지 가능합니다";
        public static final String SIZE_RANGE = "{min}자 이상 {max}자 이하여야 합니다";
        public static final String MIN_VALUE = "{value} 이상이어야 합니다";
        public static final String MAX_VALUE = "{value} 이하여야 합니다";
        public static final String RANGE = "{min} 이상 {max} 이하여야 합니다";

        // Bean Validation 메시지 키
        public static final String KEY_MIN_LENGTH = "common.min.length";
        public static final String KEY_MAX_LENGTH = "common.max.length";
        public static final String KEY_SIZE_RANGE = "common.size.range";
        public static final String KEY_MIN_VALUE = "common.min.value";
        public static final String KEY_MAX_VALUE = "common.max.value";
        public static final String KEY_RANGE = "common.range";

        // Bean Validation 템플릿
        public static final String TEMPLATE_MIN_LENGTH = ValidationMessages.toMessageTemplate(KEY_MIN_LENGTH);
        public static final String TEMPLATE_MAX_LENGTH = ValidationMessages.toMessageTemplate(KEY_MAX_LENGTH);
        public static final String TEMPLATE_SIZE_RANGE = ValidationMessages.toMessageTemplate(KEY_SIZE_RANGE);
        public static final String TEMPLATE_MIN_VALUE = ValidationMessages.toMessageTemplate(KEY_MIN_VALUE);
        public static final String TEMPLATE_MAX_VALUE = ValidationMessages.toMessageTemplate(KEY_MAX_VALUE);
        public static final String TEMPLATE_RANGE = ValidationMessages.toMessageTemplate(KEY_RANGE);
    }


    /**
     * 도서 관련 오류 메시지
     */
    public static final class Book {
        public static final String NAME_REQUIRED = "도서명은 필수입니다.";
        public static final String NAME_TOO_SHORT = "도서명은 최소 %d자 이상이어야 합니다.";
        public static final String NAME_TOO_LONG = "도서명은 최대 %d자까지 입력 가능합니다.";
        public static final String NAME_INVALID_CHARS = "도서명에 사용할 수 없는 특수문자가 포함되어 있습니다.";

        public static final String QUANTITY_TOO_SMALL = "도서 수량은 최소 %d개 이상이어야 합니다.";
        public static final String QUANTITY_TOO_LARGE = "도서 수량은 최대 %d개까지 설정 가능합니다.";

        public static final String TYPE_REQUIRED = "도서 유형은 필수입니다.";
        public static final String LANGUAGE_REQUIRED = "도서 언어는 필수입니다.";

        // 대출 관련
        public static final String ALREADY_BORROWED = "이미 대출 중인 도서입니다.";
        public static final String NOT_AVAILABLE = "현재 대출 불가능한 도서입니다.";
        public static final String INSUFFICIENT_STOCK = "재고가 부족하여 대출할 수 없습니다.";
    }

    public static final class BookName {
        private BookName() {}

        public static final String REQUIRED = "도서명은 필수입니다.";
        public static final String EMPTY = "도서명은 빈 값일 수 없습니다.";
        public static final String TOO_SHORT = "도서명은 최소 1자 이상이어야 합니다.";
        public static final String TOO_LONG = "도서명은 100자를 초과할 수 없습니다.";
    }
}