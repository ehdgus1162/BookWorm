package com.bookworm.application.dto;

import com.bookworm.domain.entity.Book;

/**
 * 도서 목록 조회용 DTO
 */
public record BookDto(
        Long id,
        String name,
        String type,
        String language,
        String availability,
        Integer availableQuantity
) {

    public static BookDto from(Book book) {
        // 재고가 있고 상태가 AVAILABLE이면 "Available", 아니면 "Borrowed"
        String availability = book.isAvailable() ? "Available" : "Borrowed";

        return new BookDto(
                book.getId(),
                book.getTitle().getValue(),
                book.getType().getValue(),
                book.getLanguage().getValue(),
                availability,
                book.getQuantity().getValue()
        );
    }
}