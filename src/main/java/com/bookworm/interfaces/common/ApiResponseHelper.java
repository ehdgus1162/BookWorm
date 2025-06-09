package com.bookworm.interfaces.common;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;

/**
 * 통일된 API 응답 헬퍼 클래스
 *
 * 개선사항:
 * 1. ✅ HTTP 헤더에 한글 포함 방지 (인코딩 에러 해결)
 * 2. 🔧 Builder 패턴 도입으로 유연성 증대
 * 3. 📊 에러 데이터 포함 가능한 오버로드 메서드 추가
 * 4. 🌐 Location 헤더 지원 (RESTful API 표준)
 * 5. 🎯 명확한 메서드 체이닝과 타입 안전성
 *
 * ⚠️ 중요: 모든 한글 메시지는 응답 본문(JSON)에만 포함됩니다.
 */
@Component
public class ApiResponseHelper {

    // ==================== 성공 응답 메서드 ====================

    /**
     * 성공 응답 - 데이터 포함
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(
                ApiResponse.success(data, message)
        );
    }

    /**
     * 성공 응답 - 데이터 없음
     */
    public static ResponseEntity<ApiResponse<Void>> ok(String message) {
        return ResponseEntity.ok(
                ApiResponse.success(null, message)
        );
    }

    /**
     * 성공 응답 - 데이터만 (기본 메시지)
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ok(data, "요청이 성공적으로 처리되었습니다.");
    }

    /**
     * 생성 성공 응답 (201 Created) - Location 헤더 포함
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message, String locationPath) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.CREATED);

        // ✅ Location 헤더는 영어/숫자만 포함되므로 안전
        if (locationPath != null && !locationPath.trim().isEmpty()) {
            try {
                builder.location(URI.create(locationPath));
            } catch (IllegalArgumentException e) {
                // Location URI 생성 실패 시 로그만 남기고 계속 진행
                System.err.println("Invalid location path: " + locationPath);
            }
        }

        return builder.body(ApiResponse.success(data, message));
    }

    /**
     * 생성 성공 응답 (201 Created) - Location 헤더 없음
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, message));
    }

    /**
     * 생성 성공 응답 - ID 기반 Location 헤더
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, Object resourceId, String message) {
        String locationPath = resourceId != null ? "/api/resource/" + resourceId : null;
        return created(data, message, locationPath);
    }

    /**
     * 성공 응답 - 내용 없음 (204 No Content)
     */
    public static ResponseEntity<ApiResponse<Void>> noContent(String message) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(null, message));
    }

    /**
     * 성공 응답 - 내용 없음 (기본 메시지)
     */
    public static ResponseEntity<ApiResponse<Void>> noContent() {
        return noContent("요청이 성공적으로 처리되었습니다.");
    }

    // ==================== 페이징 응답 메서드 ====================

    /**
     * 페이징 응답
     */
    public static <T> ResponseEntity<ApiResponse<PagedResponse<T>>> okPaged(Page<T> page, String message) {
        PagedResponse<T> pagedResponse = PagedResponse.from(page);
        return ok(pagedResponse, message);
    }

    /**
     * 페이징 응답 - 기본 메시지
     */
    public static <T> ResponseEntity<ApiResponse<PagedResponse<T>>> okPaged(Page<T> page) {
        return okPaged(page, "페이징 데이터를 성공적으로 조회했습니다.");
    }

    // ==================== 에러 응답 메서드 ====================

    /**
     * 에러 응답 - 상태 코드, 메시지, 에러 데이터 포함
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(int statusCode, String message, T errorData) {
        return ResponseEntity.status(statusCode)
                .body(ApiResponse.error(statusCode, message, errorData));
    }

    /**
     * 에러 응답 - 상태 코드와 메시지만
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(int statusCode, String message) {
        return error(statusCode, message, null);
    }

    /**
     * 에러 응답 - HttpStatus enum 사용
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
        return error(status.value(), message, null);
    }

    /**
     * 에러 응답 - HttpStatus enum과 에러 데이터
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message, T errorData) {
        return error(status.value(), message, errorData);
    }

    // ==================== 구체적인 에러 응답 메서드 ====================

    /**
     * 잘못된 요청 응답 (400 Bad Request)
     */
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 잘못된 요청 응답 - 검증 에러 포함
     */
    public static ResponseEntity<ApiResponse<Map<String, String>>> badRequest(String message, Map<String, String> fieldErrors) {
        return error(HttpStatus.BAD_REQUEST, message, fieldErrors);
    }

    /**
     * 인증 필요 응답 (401 Unauthorized)
     */
    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return error(HttpStatus.UNAUTHORIZED, message);
    }

    /**
     * 인증 필요 응답 - 기본 메시지
     */
    public static <T> ResponseEntity<ApiResponse<T>> unauthorized() {
        return unauthorized("인증이 필요합니다.");
    }

    /**
     * 권한 없음 응답 (403 Forbidden)
     */
    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return error(HttpStatus.FORBIDDEN, message);
    }

    /**
     * 권한 없음 응답 - 기본 메시지
     */
    public static <T> ResponseEntity<ApiResponse<T>> forbidden() {
        return forbidden("접근 권한이 없습니다.");
    }

    /**
     * 리소스 없음 응답 (404 Not Found)
     */
    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return error(HttpStatus.NOT_FOUND, message);
    }

    /**
     * 리소스 없음 응답 - 기본 메시지
     */
    public static <T> ResponseEntity<ApiResponse<T>> notFound() {
        return notFound("요청한 리소스를 찾을 수 없습니다.");
    }

    /**
     * 충돌 응답 (409 Conflict) - 데이터 중복 등
     */
    public static <T> ResponseEntity<ApiResponse<T>> conflict(String message) {
        return error(HttpStatus.CONFLICT, message);
    }

    /**
     * 충돌 응답 - 기본 메시지
     */
    public static <T> ResponseEntity<ApiResponse<T>> conflict() {
        return conflict("요청한 작업이 현재 리소스 상태와 충돌합니다.");
    }

    /**
     * 처리할 수 없는 엔티티 응답 (422 Unprocessable Entity)
     */
    public static <T> ResponseEntity<ApiResponse<T>> unprocessableEntity(String message) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    /**
     * 처리할 수 없는 엔티티 응답 - 검증 에러 포함
     */
    public static ResponseEntity<ApiResponse<Map<String, String>>> unprocessableEntity(String message, Map<String, String> validationErrors) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, message, validationErrors);
    }

    /**
     * 내부 서버 오류 응답 (500 Internal Server Error)
     */
    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(String message) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * 내부 서버 오류 응답 - 기본 메시지
     */
    public static <T> ResponseEntity<ApiResponse<T>> internalServerError() {
        return internalServerError("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

    /**
     * 서비스 이용 불가 응답 (503 Service Unavailable)
     */
    public static <T> ResponseEntity<ApiResponse<T>> serviceUnavailable(String message) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    /**
     * 서비스 이용 불가 응답 - 기본 메시지
     */
    public static <T> ResponseEntity<ApiResponse<T>> serviceUnavailable() {
        return serviceUnavailable("서비스를 일시적으로 이용할 수 없습니다. 잠시 후 다시 시도해주세요.");
    }

    // ==================== 유틸리티 메서드 ====================

    /**
     * 조건부 응답 - 데이터 존재 여부에 따라 200 또는 404 반환
     */
    public static <T> ResponseEntity<ApiResponse<T>> okOrNotFound(T data, String successMessage, String notFoundMessage) {
        return data != null ? ok(data, successMessage) : notFound(notFoundMessage);
    }

    /**
     * 조건부 응답 - 기본 메시지 사용
     */
    public static <T> ResponseEntity<ApiResponse<T>> okOrNotFound(T data) {
        return okOrNotFound(data,
                "요청이 성공적으로 처리되었습니다.",
                "요청한 리소스를 찾을 수 없습니다.");
    }

    /**
     * Builder 패턴을 위한 정적 메서드
     */
    public static ResponseBuilder builder() {
        return new ResponseBuilder();
    }

    /**
     * 응답 Builder 클래스
     */
    public static class ResponseBuilder {
        private HttpStatus status = HttpStatus.OK;
        private String message;
        private Object data;
        private String locationPath;

        public ResponseBuilder status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public ResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ResponseBuilder data(Object data) {
            this.data = data;
            return this;
        }

        public ResponseBuilder location(String locationPath) {
            this.locationPath = locationPath;
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> ResponseEntity<ApiResponse<T>> build() {
            ResponseEntity.BodyBuilder builder = ResponseEntity.status(status);

            if (locationPath != null && !locationPath.trim().isEmpty()) {
                try {
                    builder.location(URI.create(locationPath));
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid location path: " + locationPath);
                }
            }

            ApiResponse<T> response = status.is2xxSuccessful()
                    ? ApiResponse.success((T) data, message)
                    : ApiResponse.error(status.value(), message, (T) data);

            return builder.body(response);
        }
    }
}