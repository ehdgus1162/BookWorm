package com.bookworm.application.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * 도서 상태 변경 요청 DTO
 */
public record BookStatusChangeRequest(
        @NotBlank(message = "변경할 상태는 필수입니다.")
        @JsonProperty("status")
        String status
) {}