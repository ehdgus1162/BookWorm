package com.bookworm.domain.validation.annotation;

import com.bookworm.domain.validation.validator.PhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 전화번호 유효성 검증을 위한 커스텀 어노테이션
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface ValidPhoneNumber {
    String message() default "{custom.valid.phone}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 필수 값 여부 (기본값: true)
     */
    boolean required() default true;

    /**
     * 전화번호 패턴 (기본값: 000-0000-0000 형식)
     */
    String pattern() default "^\\d{3}-\\d{3,4}-\\d{4}$";
}