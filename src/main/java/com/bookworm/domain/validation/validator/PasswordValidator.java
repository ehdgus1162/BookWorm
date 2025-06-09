package com.bookworm.domain.validation.validator;

import com.bookworm.domain.constant.ErrorMessages;
import com.bookworm.domain.validation.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialChar;
    private boolean required;
    private Pattern specialCharPattern;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
        this.required = constraintAnnotation.required();
        this.specialCharPattern = Pattern.compile(constraintAnnotation.specialCharPattern());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation(); // 기본 메시지 비활성화

        List<String> errors = validatePassword(value);

        if (!errors.isEmpty()) {
            context.buildConstraintViolationWithTemplate(
                            String.join(", ", errors))
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    /**
     * 어노테이션 속성에 따라 비밀번호 유효성을 검증합니다.
     */
    private List<String> validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        // 필수 값 검증
        if (password == null || password.isBlank()) {
            if (required) {
                errors.add(ErrorMessages.Password.REQUIRED);
            }
            return errors; // 비밀번호가 없으면 추가 검증 불필요
        }

        // 길이 검증
        if (password.length() < minLength) {
            errors.add(String.format(ErrorMessages.Password.TOO_SHORT_TEMPLATE, minLength));
        }

        if (password.length() > maxLength) {
            errors.add(String.format(ErrorMessages.Password.TOO_LONG_TEMPLATE, maxLength));
        }

        // 복잡성 검증 - 어노테이션 속성에 따라 조건부 검증
        if (requireUppercase) {
            boolean hasUppercase = !password.equals(password.toLowerCase());
            if (!hasUppercase) {
                errors.add(ErrorMessages.Password.MISSING_UPPERCASE);
            }
        }

        if (requireLowercase) {
            boolean hasLowercase = !password.equals(password.toUpperCase());
            if (!hasLowercase) {
                errors.add(ErrorMessages.Password.MISSING_LOWERCASE);
            }
        }

        if (requireDigit) {
            boolean hasDigit = password.matches(".*\\d.*");
            if (!hasDigit) {
                errors.add(ErrorMessages.Password.MISSING_DIGIT);
            }
        }

        if (requireSpecialChar) {
            boolean hasSpecialChar = specialCharPattern.matcher(password).find();
            if (!hasSpecialChar) {
                errors.add(ErrorMessages.Password.MISSING_SPECIAL_CHAR);
            }
        }

        return errors;
    }
}