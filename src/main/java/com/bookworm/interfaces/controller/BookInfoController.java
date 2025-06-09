package com.bookworm.interfaces.controller;

import com.bookworm.application.dto.BookOptionsResponse;
import com.bookworm.application.service.book.BookQueryService;
import com.bookworm.application.service.common.BookStatisticsResponse;
import com.bookworm.interfaces.common.ApiResponse;
import com.bookworm.interfaces.common.ApiResponseHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 도서 통계 및 옵션 조회 API Controller
 * - 대시보드용 통계 데이터
 * - 폼 입력용 옵션 데이터
 * - 캐싱 적용 권장
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookInfoController {

    private final BookQueryService bookQueryService;

    /**
     * 도서 통계 조회
     * GET /api/books/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<BookStatisticsResponse>> getBookStatistics() {
        try {
            BookStatisticsResponse response = bookQueryService.getBookStatistics();
            return ApiResponseHelper.ok(response, "도서 통계 조회 성공");

        } catch (Exception e) {
            log.error("도서 통계 조회 실패: {}", e.getMessage(), e);
            return ApiResponseHelper.error(500, "도서 통계 조회에 실패했습니다.");
        }
    }

    /**
     * 도서 등록/수정용 옵션 조회
     * GET /api/books/options
     */
    @GetMapping("/options")
    public ResponseEntity<ApiResponse<BookOptionsResponse>> getBookOptions() {
        try {
            BookOptionsResponse response = bookQueryService.getBookOptions();
            return ApiResponseHelper.ok(response, "도서 옵션 조회 성공");

        } catch (Exception e) {
            log.error("도서 옵션 조회 실패: {}", e.getMessage(), e);
            return ApiResponseHelper.error(500, "도서 옵션 조회에 실패했습니다.");
        }
    }

    /**
     * 전체 도서 수 조회
     * GET /api/books/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getTotalBookCount() {
        try {
            long count = bookQueryService.getTotalBookCount();
            return ApiResponseHelper.ok(count, "전체 도서 수 조회 성공");

        } catch (Exception e) {
            log.error("전체 도서 수 조회 실패: {}", e.getMessage(), e);
            return ApiResponseHelper.error(500, "전체 도서 수 조회에 실패했습니다.");
        }
    }
}