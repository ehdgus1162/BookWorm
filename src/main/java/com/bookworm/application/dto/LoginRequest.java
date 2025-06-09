package com.bookworm.application.dto;

import com.bookworm.domain.validation.annotation.ValidEmail;
import com.bookworm.domain.validation.annotation.ValidPassword;

public record LoginRequest(
        @ValidEmail
        String email,

        @ValidPassword
        String password
) {}