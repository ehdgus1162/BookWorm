package com.bookworm.application.dto;

public record BookPopularityDto(
        Long bookId,
        String bookTitle,
        long borrowCount
) {}