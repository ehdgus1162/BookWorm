package com.bookworm.interfaces.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값은 JSON에서 제외
public class ApiResponse<T> {
    private final boolean success;
    private final int status;
    private final String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    private final T data;
    private final Object errors;

    @Builder
    private ApiResponse(boolean success, int status, String message, T data, Object errors) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.data = data;
        this.errors = errors;
    }

    // 성공 응답 생성 메서드
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, int status, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    // 실패 응답 생성 메서드
    public static <T> ApiResponse<T> error(int status, String message, Object errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .errors(errors)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return error(status, message, null);
    }

    // 간단한 에러 응답 (메시지만)
    public static <T> ApiResponse<T> error(String message) {
        return error(400, message, null);
    }
}