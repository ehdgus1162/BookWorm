package com.bookworm.application.dto;

/**
 * 사용자 활동 DTO
 */
public record UserActivityDto(
        Long userId,
        String userName,
        long totalBorrows
) {}