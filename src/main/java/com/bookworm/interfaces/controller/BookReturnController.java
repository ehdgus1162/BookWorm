package com.bookworm.interfaces.controller;

import com.bookworm.application.dto.BookReturnRequest;
import com.bookworm.application.dto.BookReturnResponse;
import com.bookworm.application.dto.BookReturnStatistics;
import com.bookworm.application.dto.LoanResponse;
import com.bookworm.application.service.Loan.BookReturnService;
import com.bookworm.domain.exception.LoanBusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 도서 반납 컨트롤러
 *
 * 설계 원리:
 * 1. RESTful API 설계 원칙 준수
 * 2. 적절한 HTTP 상태 코드 사용
 * 3. 명확한 에러 응답 구조
 * 4. 검증 로직과 비즈니스 로직의 분리
 * 5. 일관된 응답 형식 제공
 *
 * URL 충돌 해결을 위해 /api/returns 경로 사용
 */
@RestController
@RequestMapping("/api/returns")  // 경로 변경: /api/loans → /api/returns
@RequiredArgsConstructor
@Slf4j
public class BookReturnController {

    private final BookReturnService bookReturnService;

    /**
     * 도서 반납 API
     *
     * HTTP Method: PUT (리소스 상태 변경)
     * Path: /api/returns/{loanId}/process
     */
    @PutMapping("/{loanId}/process")  // 경로 변경: /return → /process
    public ResponseEntity<BookReturnResponse> returnBook(@PathVariable Long loanId) {
        log.info("도서 반납 API 호출. 대출 ID: {}", loanId);

        BookReturnRequest request = BookReturnRequest.of(loanId);
        BookReturnResponse response = bookReturnService.returnBook(request);

        return ResponseEntity.ok(response);
    }

    /**
     * 메모와 함께 도서 반납 API
     */
    @PutMapping("/{loanId}/process-with-note")  // 경로 변경
    public ResponseEntity<BookReturnResponse> returnBookWithNote(
            @PathVariable Long loanId,
            @Valid @RequestBody BookReturnRequest request) {

        log.info("메모와 함께 도서 반납 API 호출. 대출 ID: {}, 메모: {}",
                loanId, request.returnNote());

        // Path Variable과 Request Body의 loanId 일치성 검증
        if (!loanId.equals(request.loanId())) {
            return ResponseEntity.badRequest()
                    .body(BookReturnResponse.failure(loanId, "경로의 대출 ID와 요청 본문의 대출 ID가 일치하지 않습니다."));
        }

        BookReturnResponse response = bookReturnService.returnBook(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 여러 도서 일괄 반납 API
     */
    @PutMapping("/bulk-process")  // 경로 변경: /bulk-return → /bulk-process
    public ResponseEntity<List<BookReturnResponse>> returnBooks(
            @RequestBody List<Long> loanIds) {

        log.info("도서 일괄 반납 API 호출. 대출 건수: {}", loanIds != null ? loanIds.size() : 0);

        if (loanIds == null || loanIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<BookReturnResponse> responses = bookReturnService.returnBooks(loanIds);
        return ResponseEntity.ok(responses);
    }

    /**
     * 사용자의 활성 대출 목록 조회 (반납 가능한 도서들)
     */
    @GetMapping("/user/{userId}/available")  // 경로 변경: /returnable → /available
    public ResponseEntity<List<LoanResponse>> getReturnableLoans(@PathVariable Long userId) {
        log.info("반납 가능한 대출 조회 API 호출. 사용자 ID: {}", userId);

        List<LoanResponse> returnableLoans = bookReturnService.findReturnableLoans(userId);
        return ResponseEntity.ok(returnableLoans);
    }

    /**
     * 사용자의 반납 이력 조회
     */
    @GetMapping("/user/{userId}/history")  // 경로 변경: /return-history → /history
    public ResponseEntity<List<LoanResponse>> getUserReturnHistory(@PathVariable Long userId) {
        log.info("사용자 반납 이력 조회 API 호출. 사용자 ID: {}", userId);

        List<LoanResponse> returnHistory = bookReturnService.findUserReturnHistory(userId);
        return ResponseEntity.ok(returnHistory);
    }

    /**
     * 연체된 대출 목록 조회
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<LoanResponse>> getOverdueLoans() {
        log.info("연체 대출 조회 API 호출");

        List<LoanResponse> overdueLoans = bookReturnService.findOverdueLoans();
        return ResponseEntity.ok(overdueLoans);
    }

    /**
     * 곧 반납 예정인 대출 목록 조회
     */
    @GetMapping("/upcoming-due")
    public ResponseEntity<List<LoanResponse>> getUpcomingDueLoans(
            @RequestParam(defaultValue = "3") int days) {

        log.info("곧 반납 예정 대출 조회 API 호출. 조회 일수: {}", days);

        List<LoanResponse> upcomingLoans = bookReturnService.findUpcomingDueLoans(days);
        return ResponseEntity.ok(upcomingLoans);
    }

    /**
     * 대출 상세 정보 조회
     */
    @GetMapping("/loan/{loanId}/details")  // 경로 변경: /{loanId} → /loan/{loanId}/details
    public ResponseEntity<LoanResponse> getLoanDetails(@PathVariable Long loanId) {
        log.info("대출 상세 정보 조회 API 호출. 대출 ID: {}", loanId);

        LoanResponse loanDetails = bookReturnService.findLoanDetails(loanId);
        return ResponseEntity.ok(loanDetails);
    }

    /**
     * 반납 통계 조회
     */
    @GetMapping("/statistics")  // 경로 변경: /statistics/returns → /statistics
    public ResponseEntity<BookReturnStatistics> getReturnStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("반납 통계 조회 API 호출. 기간: {} ~ {}", startDate, endDate);

        BookReturnStatistics statistics = bookReturnService.getReturnStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    // ===== 예외 처리 =====

    /**
     * 비즈니스 예외 처리
     *
     * 원리: 전역 예외 처리로 일관된 에러 응답 제공
     */
    @ExceptionHandler(LoanBusinessException.class)
    public ResponseEntity<ErrorResponse> handleLoanBusinessException(LoanBusinessException e) {
        log.warn("대출 비즈니스 예외 발생: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "LOAN_BUSINESS_ERROR",
                e.getMessage(),
                System.currentTimeMillis()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 유효성 검증 예외 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("잘못된 인자 예외 발생: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_ARGUMENT",
                e.getMessage(),
                System.currentTimeMillis()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("예기치 않은 오류 발생", e);

        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다.",
                System.currentTimeMillis()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 에러 응답 DTO
     */
    public record ErrorResponse(
            String errorCode,
            String message,
            long timestamp
    ) {}
}