package com.bookworm.interfaces.exception;

import com.bookworm.domain.exception.BookBusinessException;
import com.bookworm.interfaces.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 글로벌 예외 처리기
 *
 * ⚠️ 중요: HTTP 헤더에는 절대로 한글을 포함하지 않습니다!
 * ✅ 모든 한글 메시지는 응답 본문(JSON)에만 포함됩니다.
 */
@RestControllerAdvice
@Slf4j
public class BookApiExceptionHandler {

    /**
     * Bean Validation 예외 처리 (@Valid 실패)
     *
     * ✅ 헤더 완전 제거 - 한글 인코딩 문제 해결
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e) {

        // 필드별 에러 메시지 수집
        Map<String, String> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "잘못된 값입니다",
                        (existing, replacement) -> existing
                ));

        log.warn("입력값 검증 실패: {}", fieldErrors);

        // ✅ 헤더 없이 응답 본문에만 한글 포함
        ApiResponse<Map<String, String>> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "입력값이 올바르지 않습니다.",
                fieldErrors
        );

        // ✅ 헤더 설정 완전 제거
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 도메인 비즈니스 예외 처리
     */
    @ExceptionHandler(BookBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBookBusinessException(BookBusinessException e) {
        log.warn("도서 비즈니스 예외: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Bind 예외 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(BindException e) {

        Map<String, String> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "잘못된 값입니다",
                        (existing, replacement) -> existing
                ));

        log.warn("데이터 바인딩 실패: {}", fieldErrors);

        ApiResponse<Map<String, String>> response = fieldErrors.isEmpty()
                ? ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "요청 데이터 형식이 올바르지 않습니다.",
                null
        )
                : ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "입력값이 올바르지 않습니다.",
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Constraint Violation 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException e) {

        Map<String, String> violations = e.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> getPropertyPath(violation),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));

        log.warn("제약 조건 위반: {}", violations);

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "입력값이 제약 조건을 위반했습니다.",
                violations
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("잘못된 인수: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage() != null ? e.getMessage() : "잘못된 요청입니다.",
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 예상치 못한 서버 오류 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("예상치 못한 서버 오류: {}", e.getMessage(), e);

        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                null
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * ConstraintViolation에서 프로퍼티 경로 추출
     */
    private String getPropertyPath(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        int lastDotIndex = propertyPath.lastIndexOf('.');
        return lastDotIndex >= 0 ? propertyPath.substring(lastDotIndex + 1) : propertyPath;
    }
}