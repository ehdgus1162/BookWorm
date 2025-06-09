package com.bookworm.domain.validation.validator;

import com.bookworm.domain.validation.annotation.EnumConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumValidator implements ConstraintValidator<EnumConstraint, String> {

    private Set<String> acceptedValues;
    private boolean ignoreCase;
    private boolean nullable;

    @Override
    public void initialize(EnumConstraint constraintAnnotation) {
        Class<? extends Enum<?>> enumClass = constraintAnnotation.enumClass();
        this.ignoreCase = constraintAnnotation.ignoreCase();
        this.nullable = constraintAnnotation.nullable();

        this.acceptedValues = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .map(name -> ignoreCase ? name.toLowerCase() : name)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return nullable;
        }

        String valueToCheck = ignoreCase ? value.toLowerCase() : value;
        return acceptedValues.contains(valueToCheck);
    }
}
