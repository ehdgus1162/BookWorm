package com.bookworm.domain.vo.validator;

import com.bookworm.common.error.ErrorMessage;
import com.bookworm.domain.vo.exception.PasswordException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordValidatorImpl implements PasswordValidator {
    // 비밀번호 유효성 검증을 위한 정규 표현식
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    @Override
    public void validate(String password)
            throws PasswordException {
        // 에러 메시지를 담을 리스트
        List<String> violations = new ArrayList<>();

        // null 값 우선 체크
        if (password == null || password.isBlank()) {
            throw new PasswordException.EmptyPasswordException();
        }

        if (password.length() < MIN_LENGTH) {
            violations.add(ErrorMessage.Password.LENGTH);
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            violations.add(ErrorMessage.Password.UPPERCASE);
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            violations.add(ErrorMessage.Password.DIGIT);
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            violations.add(ErrorMessage.Password.SPECIAL_CHAR);
        }

        // 에러 메세지가 있으면 예외를 리스트에 담아서 던짐
        if (!violations.isEmpty()) {
            throw new PasswordException.InvalidPasswordFormatException(violations);
        }
    }

    @Override
    public boolean isValid(String password) {
        try {
            validate(password);
            return true;
        } catch (PasswordException e) {
            return false;
        }
    }
}
