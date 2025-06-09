package com.bookworm.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

/**
 * 도서 등록 요청 DTO
 */
public record CreateBookRequest(
        @NotBlank(message = "도서 제목은 필수입니다.")
        @Size(max = 200, message = "도서 제목은 200자를 초과할 수 없습니다.")
        @JsonProperty("title")
        String title,

        @NotBlank(message = "도서 언어는 필수입니다.")
        @JsonProperty("language")
        String language,

        @NotBlank(message = "도서 유형은 필수입니다.")
        @JsonProperty("type")
        String type,

        @NotNull(message = "도서 수량은 필수입니다.")
        @Min(value = 1, message = "도서 수량은 1 이상이어야 합니다.")
        @Max(value = 9999, message = "도서 수량은 9999개를 초과할 수 없습니다.")
        @JsonProperty("quantity")
        Integer quantity
) {}