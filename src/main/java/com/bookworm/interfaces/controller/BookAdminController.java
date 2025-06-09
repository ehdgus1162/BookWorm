package com.bookworm.interfaces.controller;

import com.bookworm.application.dto.BookResponse;
import com.bookworm.application.service.book.BookQueryService;
import com.bookworm.interfaces.common.ApiResponse;
import com.bookworm.interfaces.common.ApiResponseHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication; // 이 import 추가!


import java.util.List;

/**
 * 관리자별 도서 조회 API Controller
 * - 특정 관리자가 등록한 도서 조회
 * - 현재 로그인한 관리자의 등록 도서 조회
 */
@RestController
@RequestMapping("/api/books/admin")
@RequiredArgsConstructor
@Slf4j
public class BookAdminController {

    private final BookQueryService bookQueryService;

    /**
     * 현재 로그인한 관리자가 등록한 도서 조회
     * GET /api/books/admin/my-books
     */
    @GetMapping("/my-books")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getMyBooks(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<BookResponse> response = bookQueryService.getBooksRegisteredBy(userEmail);

            return ApiResponseHelper.ok(response, "내가 등록한 도서 목록 조회 성공");

        } catch (Exception e) {
            log.error("내가 등록한 도서 목록 조회 실패: {}", e.getMessage(), e);
            return ApiResponseHelper.error(500, "내가 등록한 도서 목록 조회에 실패했습니다.");
        }
    }

    /**
     * 특정 관리자가 등록한 도서 조회 (관리자용)
     * GET /api/books/admin/by-email?email=admin@example.com
     */
    @GetMapping("/by-email")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksByAdmin(
            @RequestParam String email) {

        try {
            List<BookResponse> response = bookQueryService.getBooksRegisteredBy(email);
            return ApiResponseHelper.ok(response, "관리자별 도서 목록 조회 성공");

        } catch (IllegalArgumentException e) {
            log.warn("관리자별 도서 조회 요청 오류 - Email: {}, 원인: {}", email, e.getMessage());
            return ApiResponseHelper.badRequest("유효하지 않은 관리자 이메일입니다.");

        } catch (Exception e) {
            log.error("관리자별 도서 목록 조회 실패 - Email: {}: {}", email, e.getMessage(), e);
            return ApiResponseHelper.error(500, "관리자별 도서 목록 조회에 실패했습니다.");
        }
    }
}