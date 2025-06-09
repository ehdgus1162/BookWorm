package com.bookworm.application.dto;

import com.bookworm.domain.validation.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@ValidAddress(
        street = "street",
        city = "city",
        state = "state",
        country = "country"
)
public record SignUpRequest(
        @ValidEmail
        String email,

        @ValidPassword
        String password,

        @ValidName(isFirstName = true)
        String firstName,

        @ValidName(isFirstName = false)
        String lastName,

        String street,
        String city,
        String state,
        String country,

        @ValidPhoneNumber
        String phoneNumber
) {}
