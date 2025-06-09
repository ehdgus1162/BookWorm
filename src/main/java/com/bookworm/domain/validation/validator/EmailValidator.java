package com.bookworm.domain.validation.validator;

import com.bookworm.domain.validation.annotation.ValidEmail;
import com.bookworm.domain.validation.utils.EmailValidationUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

/**
 * 이메일 유효성 검증 Validator
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        // 초기화 로직 (필요시)
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null이나 빈 문자열은 다른 어노테이션(@NotNull, @NotBlank)에서 처리
        if (value == null || value.trim().isEmpty()) {
            return true; // @ValidEmail은 값이 있을 때만 형식 검증
        }

        try {
            // 방법 1: 예외 발생하지 않는 검증 메서드 사용
            List<String> errors = EmailValidationUtils.validateEmail(value);

            if (!errors.isEmpty()) {
                // 기본 메시지 비활성화하고 커스텀 메시지 추가
                context.disableDefaultConstraintViolation();

                // 첫 번째 오류 메시지만 사용 (여러 오류가 있을 경우)
                context.buildConstraintViolationWithTemplate(errors.get(0))
                        .addConstraintViolation();

                return false;
            }

            return true;

        } catch (Exception e) {
            // 예상치 못한 예외 발생 시 검증 실패 처리
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("이메일 검증 중 오류가 발생했습니다.")
                    .addConstraintViolation();
            return false;
        }
    }
}