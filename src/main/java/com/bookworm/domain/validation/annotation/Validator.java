package com.bookworm.domain.validation.annotation;

public interface Validator<T> {
    ValidationResult<T> validate(T input);
}
