package com.bookworm.domain.validation.annotation;

import com.bookworm.domain.constant.ErrorMessages;
import com.bookworm.domain.validation.validator.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 비밀번호 복잡성 요구사항을 검증하는 어노테이션
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface ValidPassword {
    String message() default ErrorMessages.CustomAnnotation.PASSWORD_DEFAULT;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 필수 값 여부 (기본값: true)
     */
    boolean required() default true;

    /**
     * 최소 길이 (기본값: 8)
     */
    int minLength() default 8;

    /**
     * 최대 길이 (기본값: 100)
     */
    int maxLength() default 100;

    /**
     * 대문자 포함 여부 (기본값: true)
     */
    boolean requireUppercase() default true;

    /**
     * 소문자 포함 여부 (기본값: true)
     */
    boolean requireLowercase() default true;

    /**
     * 숫자 포함 여부 (기본값: true)
     */
    boolean requireDigit() default true;

    /**
     * 특수 문자 포함 여부 (기본값: true)
     */
    boolean requireSpecialChar() default true;

    /**
     * 허용되는 특수 문자 패턴 (기본값: 일반적인 특수 문자들)
     */
    String specialCharPattern() default "[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]";
}