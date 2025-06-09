package com.bookworm.interfaces.controller;


import com.bookworm.application.dto.*;
import com.bookworm.application.service.Loan.LoanService;
import com.bookworm.domain.exception.LoanBusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 간단한 대출 컨트롤러
 * - 핵심 기능만 제공
 * - 기본적인 예외 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    // ==================== 대출 생성 ====================

    /**
     * 여러 도서 동시 대출
     */
    @PostMapping("/borrow")
    public ResponseEntity<BorrowResponseDto> borrowBooks(@Valid @RequestBody BorrowRequestDto request) {
        log.info("도서 대출 API 호출 - 사용자 ID: {}, 도서 수: {}",
                request.userId(), request.bookIds().size());

        try {
            BorrowResponseDto response = loanService.borrowBooks(request);
            return ResponseEntity.ok(response);
        } catch (LoanBusinessException e) {
            log.warn("도서 대출 실패 - 사용자 ID: {}, 오류: {}", request.userId(), e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 단일 도서 대출
     */
    @PostMapping("/borrow-single")
    public ResponseEntity<LoanResponse> borrowSingleBook(@Valid @RequestBody SingleLoanRequest request) {
        log.info("단일 도서 대출 API 호출 - 도서 ID: {}, 사용자 ID: {}",
                request.bookId(), request.userId());

        try {
            LoanResponse response = loanService.borrowSingleBook(request);
            return ResponseEntity.ok(response);
        } catch (LoanBusinessException e) {
            log.warn("단일 도서 대출 실패 - 도서 ID: {}, 오류: {}", request.bookId(), e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ==================== 대출 관리 ====================

    /**
     * 도서 반납
     */
    @PutMapping("/{loanId}/return")
    public ResponseEntity<LoanResponse> returnBook(@PathVariable Long loanId) {
        log.info("도서 반납 API 호출 - 대출 ID: {}", loanId);

        try {
            LoanResponse response = loanService.returnBook(loanId);
            return ResponseEntity.ok(response);
        } catch (LoanBusinessException e) {
            log.warn("도서 반납 실패 - 대출 ID: {}, 오류: {}", loanId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 대출 연장
     */
    @PutMapping("/{loanId}/extend")
    public ResponseEntity<LoanResponse> extendLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody ExtendLoanRequest request) {

        log.info("대출 연장 API 호출 - 대출 ID: {}, 연장 일수: {}일",
                loanId, request.extensionDays());

        try {
            LoanResponse response = loanService.extendLoan(loanId, request);
            return ResponseEntity.ok(response);
        } catch (LoanBusinessException e) {
            log.warn("대출 연장 실패 - 대출 ID: {}, 오류: {}", loanId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ==================== 대출 조회 ====================

    /**
     * 대출 상세 조회
     */
    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> getLoan(@PathVariable Long loanId) {
        try {
            LoanResponse response = loanService.getLoan(loanId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("대출 조회 실패 - 대출 ID: {}, 오류: {}", loanId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 모든 대출 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<Page<LoanResponse>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Page<LoanResponse> response = loanService.getAllLoans(page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("대출 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자별 대출 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanResponse>> getUserLoans(@PathVariable Long userId) {
        try {
            List<LoanResponse> response = loanService.getUserLoans(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("사용자 대출 조회 실패 - 사용자 ID: {}, 오류: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 활성 대출 조회
     */
    @GetMapping("/active")
    public ResponseEntity<List<LoanResponse>> getActiveLoans() {
        try {
            List<LoanResponse> response = loanService.getActiveLoans();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("활성 대출 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 연체 대출 조회
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<LoanResponse>> getOverdueLoans() {
        try {
            List<LoanResponse> response = loanService.getOverdueLoans();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("연체 대출 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== 예외 처리 ====================

    /**
     * 대출 비즈니스 예외 처리
     */
    @ExceptionHandler(LoanBusinessException.class)
    public ResponseEntity<ErrorResponse> handleLoanBusinessException(LoanBusinessException e) {
        log.warn("대출 비즈니스 오류: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "LOAN_BUSINESS_ERROR",
                e.getMessage(),
                System.currentTimeMillis()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 일반적인 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("대출 API 처리 중 오류 발생", e);

        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다.",
                System.currentTimeMillis()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 오류 응답 DTO
     */
    public record ErrorResponse(
            String errorCode,
            String message,
            long timestamp
    ) {}
}