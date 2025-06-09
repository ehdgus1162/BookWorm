package com.bookworm.interfaces.exception;

import com.bookworm.domain.exception.AuthenticationException;
import com.bookworm.domain.exception.DuplicateUserException;
import com.bookworm.domain.exception.InvalidEmailException;
import com.bookworm.interfaces.exception.ApiError;
import com.bookworm.interfaces.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기
 * 모든 Controller에서 발생하는 예외를 일관성 있게 처리
 *
 * 장점:
 * 1. Controller 코드에서 try-catch 블록 제거 가능
 * 2. 일관된 에러 응답 형식 보장
 * 3. 예외 처리 로직의 중앙집중화
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation 예외 처리
     * @Valid 어노테이션으로 인한 유효성 검증 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("유효성 검증 실패: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError
                    ? ((FieldError) error).getField()
                    : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "입력값 검증에 실패했습니다.",
                LocalDateTime.now(),
                errors
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    /**
     * 인증 실패 예외 처리
     * 로그인 시 이메일/비밀번호 불일치, 계정 비활성화 등
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        String requestURI = request.getRequestURI();
        String clientIp = getClientIpAddress(request);

        log.warn("인증 실패: 경로={}, IP={}, 메시지={}", requestURI, clientIp, ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("authentication", ex.getMessage());

        ApiError apiError = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "인증에 실패했습니다.",
                LocalDateTime.now(),
                errors
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    /**
     * 중복 사용자 예외 처리
     */
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiError> handleDuplicateUserException(DuplicateUserException ex) {
        log.warn("중복 사용자 등록 시도: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("email", ex.getMessage());

        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT.value(),
                "이미 존재하는 리소스입니다.",
                LocalDateTime.now(),
                errors
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    /**
     * 잘못된 이메일 형식 예외 처리
     */
    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ApiError> handleInvalidEmailException(InvalidEmailException ex) {
        log.warn("잘못된 이메일 형식: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("email", ex.getMessage());

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "잘못된 이메일 형식입니다.",
                LocalDateTime.now(),
                errors
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    /**
     * 리소스를 찾을 수 없는 예외 처리
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("리소스를 찾을 수 없음: {}", ex.getMessage());

        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "요청한 리소스를 찾을 수 없습니다.",
                LocalDateTime.now(),
                Map.of("message", ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    /**
     * 예상치 못한 예외 처리
     * 모든 예외의 최종 처리점
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGlobalException(Exception ex) {
        log.error("예상치 못한 오류 발생", ex);

        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "서버 내부 오류가 발생했습니다.",
                LocalDateTime.now(),
                Map.of("message", "관리자에게 문의하세요.")
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    /**
     * 클라이언트 IP 주소 추출 (프록시 환경 고려)
     * 보안 로깅을 위해 사용
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}