package com.bookworm.domain.validation.annotation;

import com.bookworm.domain.validation.validator.AddressValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 주소 유효성 검증을 위한 클래스 수준 어노테이션
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AddressValidator.class)
public @interface ValidAddress {
    String message() default "{custom.valid.address}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 도로명 필드 이름
     */
    String street() default "street";

    /**
     * 도시명 필드 이름
     */
    String city() default "city";

    /**
     * 주/도 필드 이름
     */
    String state() default "state";

    /**
     * 국가 필드 이름
     */
    String country() default "country";
}