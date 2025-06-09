package com.bookworm.interfaces.controller;


import com.bookworm.application.dto.*;
import com.bookworm.application.service.book.BookCommandService;
import com.bookworm.application.service.book.BookQueryService;
import com.bookworm.interfaces.common.ApiResponse;
import com.bookworm.interfaces.common.ApiResponseHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 도서 관리 REST API Controller
 * - CQRS 패턴 적용 (Command/Query 서비스 분리)
 * - 기존 ApiResponseHelper 활용
 * - JSR-303 검증 자동 적용
 * - 관리자 전용 API
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookCommandService bookCommandService;
    private final BookQueryService bookQueryService;

    // === 조회 API (Query) ===

    /**
     * 도서 목록 조회 (페이징)
     * GET /api/books?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<BookPageResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        try {
            BookPageResponse response = bookQueryService.getAllBooks(page, size);
            return ApiResponseHelper.ok(response, "도서 목록 조회 성공");

        } catch (Exception e) {
            log.error("도서 목록 조회 실패: {}", e.getMessage(), e);
            return ApiResponseHelper.error(500, "도서 목록 조회에 실패했습니다.");
        }
    }

    /**
     * 도서 검색 (통합 검색)
     * POST /api/books/search
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<BookPageResponse>> searchBooks(
            @Valid @RequestBody BookSearchRequest request) {

        try {
            BookPageResponse response = bookQueryService.searchBooks(request);
            return ApiResponseHelper.ok(response, "도서 검색 성공");

        } catch (IllegalArgumentException e) {
            log.warn("도서 검색 요청 오류: {}", e.getMessage());
            return ApiResponseHelper.badRequest(e.getMessage());

        } catch (Exception e) {
            log.error("도서 검색 실패: {}", e.getMessage(), e);
            return ApiResponseHelper.error(500, "도서 검색에 실패했습니다.");
        }
    }

    /**
     * 도서 상세 조회
     * GET /api/books/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBook(@PathVariable Long id) {
        try {
            BookResponse response = bookQueryService.getBook(id);
            return ApiResponseHelper.ok(response, "도서 조회 성공");

        } catch (IllegalArgumentException e) {
            log.warn("도서 조회 실패 - ID: {}, 원인: {}", id, e.getMessage());
            return ApiResponseHelper.notFound("도서를 찾을 수 없습니다.");

        } catch (Exception e) {
            log.error("도서 조회 실패 - ID: {}: {}", id, e.getMessage(), e);
            return ApiResponseHelper.error(500, "도서 조회에 실패했습니다.");
        }
    }

    /**
     * 이용 가능한 도서 목록 조회
     * GET /api/books/available
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getAvailableBooks() {
        try {
            List<BookResponse> response = bookQueryService.getAvailableBooks();
            return ApiResponseHelper.ok(response, "이용 가능한 도서 목록 조회 성공");

        } catch (Exception e) {
            log.error("이용 가능한 도서 목록 조회 실패: {}", e.getMessage(), e);
            return ApiResponseHelper.error(500, "이용 가능한 도서 목록 조회에 실패했습니다.");
        }
    }

    /**
     * 대출 중인 도서 목록 조회
     * GET /api/books/borrowed
     */
    @GetMapping("/borrowed")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBorrowedBooks() {
        try {
            List<BookResponse> response = bookQueryService.getBorrowedBooks();
            return ApiResponseHelper.ok(response, "대출 중인 도서 목록 조회 성공");

        } catch (Exception e) {
            log.error("대출 중인 도서 목록 조회 실패: {}", e.getMessage(), e);
            return ApiResponseHelper.error(500, "대출 중인 도서 목록 조회에 실패했습니다.");
        }
    }

    // === 생성/수정/삭제 API (Command) ===

    /**
     * 새로운 도서 등록
     * POST /api/books
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @Valid @RequestBody CreateBookRequest request,
            Authentication authentication) {

        try {
            String registeredByEmail = authentication.getName();
            BookResponse response = bookCommandService.createBook(request, registeredByEmail);

            return ApiResponseHelper.created(response, "도서 등록 성공");

        } catch (IllegalArgumentException e) {
            log.warn("도서 등록 요청 오류: {}", e.getMessage());
            return ApiResponseHelper.badRequest(e.getMessage());

        } catch (Exception e) {
            log.error("도서 등록 실패: {}", e.getMessage(), e);
            return ApiResponseHelper.error(500, "도서 등록에 실패했습니다.");
        }
    }

    /**
     * 도서 정보 수정
     * PUT /api/books/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest request) {

        try {
            BookResponse response = bookCommandService.updateBook(id, request);
            return ApiResponseHelper.ok(response, "도서 정보 수정 성공");

        } catch (IllegalArgumentException e) {
            log.warn("도서 수정 요청 오류 - ID: {}, 원인: {}", id, e.getMessage());
            return ApiResponseHelper.badRequest(e.getMessage());

        } catch (Exception e) {
            log.error("도서 수정 실패 - ID: {}: {}", id, e.getMessage(), e);
            return ApiResponseHelper.error(500, "도서 수정에 실패했습니다.");
        }
    }

    /**
     * 도서 삭제
     * DELETE /api/books/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        try {
            bookCommandService.deleteBook(id);
            return ApiResponseHelper.ok("도서 삭제 성공");

        } catch (IllegalArgumentException e) {
            log.warn("도서 삭제 요청 오류 - ID: {}, 원인: {}", id, e.getMessage());
            return ApiResponseHelper.badRequest(e.getMessage());

        } catch (Exception e) {
            log.error("도서 삭제 실패 - ID: {}: {}", id, e.getMessage(), e);
            return ApiResponseHelper.error(500, "도서 삭제에 실패했습니다.");
        }
    }

    /**
     * 도서 상태 변경
     * PATCH /api/books/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BookResponse>> changeBookStatus(
            @PathVariable Long id,
            @Valid @RequestBody BookStatusChangeRequest request) {

        try {
            BookResponse response = bookCommandService.changeBookStatus(id, request);
            return ApiResponseHelper.ok(response, "도서 상태 변경 성공");

        } catch (IllegalArgumentException e) {
            log.warn("도서 상태 변경 요청 오류 - ID: {}, 원인: {}", id, e.getMessage());
            return ApiResponseHelper.badRequest(e.getMessage());

        } catch (Exception e) {
            log.error("도서 상태 변경 실패 - ID: {}: {}", id, e.getMessage(), e);
            return ApiResponseHelper.error(500, "도서 상태 변경에 실패했습니다.");
        }
    }

    /**
     * 도서 재고 추가
     * PATCH /api/books/{id}/stock
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<BookResponse>> addBookStock(
            @PathVariable Long id,
            @Valid @RequestBody AddStockRequest request) {

        try {
            BookResponse response = bookCommandService.addBookStock(id, request);
            return ApiResponseHelper.ok(response, "도서 재고 추가 성공");

        } catch (IllegalArgumentException e) {
            log.warn("도서 재고 추가 요청 오류 - ID: {}, 원인: {}", id, e.getMessage());
            return ApiResponseHelper.badRequest(e.getMessage());

        } catch (Exception e) {
            log.error("도서 재고 추가 실패 - ID: {}: {}", id, e.getMessage(), e);
            return ApiResponseHelper.error(500, "도서 재고 추가에 실패했습니다.");
        }
    }
}