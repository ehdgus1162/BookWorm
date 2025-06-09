package com.bookworm.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 페이징된 도서 목록 응답 DTO
 */
public record BookPageResponse(
        @JsonProperty("content")
        java.util.List<BookResponse> content,

        @JsonProperty("totalElements")
        Long totalElements,

        @JsonProperty("totalPages")
        Integer totalPages,

        @JsonProperty("currentPage")
        Integer currentPage,

        @JsonProperty("size")
        Integer size,

        @JsonProperty("hasNext")
        Boolean hasNext,

        @JsonProperty("hasPrevious")
        Boolean hasPrevious
) {
    /**
     * Page<Book>으로부터 BookPageResponse 생성
     */
    public static BookPageResponse from(org.springframework.data.domain.Page<com.bookworm.domain.entity.Book> page) {
        java.util.List<BookResponse> content = page.getContent().stream()
                .map(BookResponse::from)
                .toList();

        return new BookPageResponse(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}