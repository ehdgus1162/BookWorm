package com.bookworm.interfaces.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * HTTP 응답을 일관성 있게 생성하는 헬퍼 클래스
 * 기존 ApiResponse 클래스를 활용하여 중복 코드 제거
 */
@Component
public class ResponseHelper {

    /**
     * 생성 응답 생성 (201 CREATED)
     * Location 헤더와 함께 생성된 리소스 정보 반환
     */
    public <T> ResponseEntity<ApiResponse<T>> created(T data, Long resourceId, String message) {
        // Location 헤더 생성
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(resourceId)
                .toUri();

        // ApiResponse 생성 (기존 success 메서드 활용)
        ApiResponse<T> response = ApiResponse.success(
                data,
                HttpStatus.CREATED.value(),
                message
        );

        return ResponseEntity.created(location).body(response);
    }

    /**
     * 콘텐츠 없음 응답 생성 (204 NO CONTENT)
     */
    public ResponseEntity<ApiResponse<Void>> noContent(String message) {
        ApiResponse<Void> response = ApiResponse.success(
                null,
                HttpStatus.NO_CONTENT.value(),
                message
        );

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}