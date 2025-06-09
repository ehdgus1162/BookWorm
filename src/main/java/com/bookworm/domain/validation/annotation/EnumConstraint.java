package com.bookworm.domain.validation.annotation;


import com.bookworm.domain.validation.validator.EnumValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enum 값 검증을 위한 커스텀 어노테이션
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidator.class)
@Documented
public @interface EnumConstraint {

    String message() default "허용되지 않는 값입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Enum<?>> enumClass();

    boolean ignoreCase() default true;

    boolean nullable() default true;
}