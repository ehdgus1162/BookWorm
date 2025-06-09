package com.bookworm.domain.validation.annotation;

import java.util.List;

public sealed interface ValidationResult<T>
        permits ValidationResult.Success, ValidationResult.Failure {

    boolean isValid();

    record Success<T>(T value) implements ValidationResult<T> {
        @Override
        public boolean isValid() { return true; }
    }

    record Failure<T>(List<String> errors) implements ValidationResult<T> {
        public Failure(String error) { this(List.of(error)); }

        @Override
        public boolean isValid() { return false; }
    }
}

