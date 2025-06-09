package com.bookworm.domain.validation.annotation;

import com.bookworm.domain.validation.validator.EmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 이메일 유효성 검증을 위한 커스텀 어노테이션
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidator.class)
public @interface ValidEmail {
    String message() default "{custom.valid.email}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 필수 값 여부 (기본값: true)
     */
    boolean required() default true;

    /**
     * 최대 길이 (기본값: 100)
     */
    int maxLength() default 100;
}