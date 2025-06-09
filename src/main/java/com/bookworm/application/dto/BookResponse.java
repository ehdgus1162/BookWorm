package com.bookworm.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 도서 응답 DTO
 */
public record BookResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("title")
        String title,

        @JsonProperty("language")
        String language,

        @JsonProperty("type")
        String type,

        @JsonProperty("quantity")
        Integer quantity,

        @JsonProperty("status")
        String status,

        @JsonProperty("statusDescription")
        String statusDescription,

        @JsonProperty("registeredBy")
        String registeredBy,

        @JsonProperty("registeredAt")
        String registeredAt,

        @JsonProperty("isAvailable")
        Boolean isAvailable,

        @JsonProperty("canBorrow")
        Boolean canBorrow
) {
    /**
     * Book 엔티티로부터 BookResponse 생성
     */
    public static BookResponse from(com.bookworm.domain.entity.Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle().getValue(),
                book.getLanguage().getValue(),
                book.getType().getValue(),
                book.getQuantity().getValue(),
                book.getStatus().name(),
                book.getStatus().getDescription(),
                book.getRegisteredBy().getFullName(),
                book.getCreatedAt() != null ? book.getCreatedAt().toString() : null,
                book.isAvailable(),
                book.canBorrow(1)
        );
    }
}