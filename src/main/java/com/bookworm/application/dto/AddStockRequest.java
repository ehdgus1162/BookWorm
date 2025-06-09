package com.bookworm.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 재고 추가 요청 DTO
 */
public record AddStockRequest(
        @NotNull(message = "추가할 수량은 필수입니다.")
        @Min(value = 1, message = "추가할 수량은 1 이상이어야 합니다.")
        @Max(value = 9999, message = "추가할 수량은 9999개를 초과할 수 없습니다.")
        @JsonProperty("additionalQuantity")
        Integer additionalQuantity
) {}