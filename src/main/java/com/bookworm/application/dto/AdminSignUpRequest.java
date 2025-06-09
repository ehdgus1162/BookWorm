package com.bookworm.application.dto;

// AdminSignUpRequest가 이런 구조라면
public record AdminSignUpRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String phoneNumber,  // ← 있다면 사용
        String street,       // ← 있다면 사용
        String city,         // ← 있다면 사용
        String state,        // ← 있다면 사용
        String country       // ← 있다면 사용
) {}