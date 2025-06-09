package com.bookworm.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 도서 검색 요청 DTO
 */
public record BookSearchRequest(
        @JsonProperty("titleKeyword")
        String titleKeyword,

        @JsonProperty("type")
        String type,

        @JsonProperty("language")
        String language,

        @JsonProperty("status")
        String status,

        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
        @JsonProperty("page")
        Integer page,

        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
        @JsonProperty("size")
        Integer size
) {
    public BookSearchRequest {
        // 기본값 설정
        if (page == null) page = 0;
        if (size == null) size = 20;
    }
}