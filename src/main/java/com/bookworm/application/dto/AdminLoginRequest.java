package com.bookworm.application.dto;

import com.bookworm.domain.validation.annotation.ValidEmail;
import com.bookworm.domain.validation.annotation.ValidPassword;

public record AdminLoginRequest(
        @ValidEmail
        String email,

        @ValidPassword
        String password
) {}