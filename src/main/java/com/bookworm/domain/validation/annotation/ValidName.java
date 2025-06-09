package com.bookworm.domain.validation.annotation;

import com.bookworm.domain.validation.validator.NameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이름/성 유효성 검증을 위한 커스텀 어노테이션
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NameValidator.class)
public @interface ValidName {
    String message() default "{custom.valid.name}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 이름(true) 또는 성(false) 여부
     */
    boolean isFirstName() default true;

    /**
     * 필수 값 여부 (기본값: true)
     */
    boolean required() default true;

    /**
     * 최대 길이 (기본값: 50)
     */
    int maxLength() default 50;
}