package com.bookworm.common.error;

public class ErrorMessage {

    private ErrorMessage() {} // 유틸 클래스라 생성자 막음

    public static final class Email {
        public static final String EMPTY = "이메일은 빈 값일 수 없습니다";
        public static final String INVALID = "유효하지 않은 이메일 형식입니다";
        public static final String DOMAIN_TOO_LONG = "이메일 도메인 부분이 너무 깁니다";
    }

    public static final class Password {
        public static final String LENGTH = "비밀번호는 최소 8자 이상이어야 합니다";
        public static final String UPPERCASE = "비밀번호는 적어도 하나의 대문자를 포함해야 합니다";
        public static final String DIGIT = "비밀번호는 적어도 하나의 숫자를 포함해야 합니다";
        public static final String SPECIAL_CHAR = "비밀번호는 적어도 하나의 특수문자를 포함해야 합니다";
        public static final String REQUIRED = "비밀번호는 필수 값입니다";
        public static final String MISMATCH = "비밀번호가 일치하지 않습니다";
        public static final String ENCRYPT_REQUIRED = "암호화된 비밀번호는 필수 값입니다";
        public static final String ENCRYPT_INVALID = "유효하지 않은 암호화된 비밀번호 형식입니다";


        public static final String ENCRYPTION_FAILED = "비밀번호 암호화 중 오류가 발생했습니다";
        public static final String MATCH_FAILED = "비밀번호 검증 중 오류가 발생했습니다";
    }

    public static final class Name {
        public static final String EMPTY = "이름은 빈 값일 수 없습니다";
        public static final String FIRST_NAME_LENGTH = "이름은 %d자 이상 %d자 이하여야 합니다";
        public static final String LAST_NAME_LENGTH = "성은 %d자 이상 %d자 이하여야 합니다";
    }

    public static final class User {
        public static final String NOT_FOUND_BY_ID = "사용자를 찾을 수 없습니다: %d";
        public static final String INVALID_PASSWORD = "비밀번호가 일치하지 않습니다";
        public static final String NOT_FOUND_BY_EMAIL = "해당 이메일을 가진 사용자를 찾을 수 없습니다: %s";
    }
}