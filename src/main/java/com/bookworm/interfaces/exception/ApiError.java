package com.bookworm.interfaces.exception;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ApiError {
    private int status;             // HTTP 상태 코드
    private String message;         // 일반적인 오류 메시지

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp; // 오류 발생 시간

    private Map<String, String> errors; // 필드별 오류 메시지
}