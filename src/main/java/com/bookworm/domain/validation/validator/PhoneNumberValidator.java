package com.bookworm.domain.validation.validator;

import com.bookworm.domain.validation.annotation.ValidPhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * ValidPhoneNumber 어노테이션을 처리하는 검증기
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private boolean required;
    private Pattern pattern;

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        this.required = constraintAnnotation.required();
        this.pattern = Pattern.compile(constraintAnnotation.pattern());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null이나 빈 값이 허용되는 경우(required=false일 때), 값이 없으면 검증 통과
        if ((value == null || value.isBlank()) && !required) {
            return true;
        }

        // 필수 값 검증
        if (value == null || value.isBlank()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "전화번호는 필수입니다.")
                    .addConstraintViolation();
            return false;
        }

        // 형식 검증
        if (!pattern.matcher(value).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "유효하지 않은 전화번호 형식입니다. 형식: 000-0000-0000")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}