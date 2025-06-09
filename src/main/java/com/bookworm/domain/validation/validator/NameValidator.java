package com.bookworm.domain.validation.validator;
import com.bookworm.domain.validation.annotation.ValidName;
import com.bookworm.domain.validation.utils.NameValidationUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

/**
 * ValidName 어노테이션을 처리하는 검증기
 */
public class NameValidator implements ConstraintValidator<ValidName, String> {

    private boolean isFirstName;
    private boolean required;

    @Override
    public void initialize(ValidName constraintAnnotation) {
        this.isFirstName = constraintAnnotation.isFirstName();
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null이나 빈 값이 허용되는 경우(required=false일 때), 값이 없으면 검증 통과
        if ((value == null || value.isBlank()) && !required) {
            return true;
        }

        // 유틸리티 클래스를 사용하여 검증
        List<String> errors = NameValidationUtils.validateName(value, isFirstName);

        if (!errors.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            String.join(", ", errors))
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}