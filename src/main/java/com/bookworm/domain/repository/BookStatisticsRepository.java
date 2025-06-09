package com.bookworm.domain.repository;

import java.util.Map;

/**
 * 도서 통계 전용 Repository
 * - 대시보드나 리포트용 통계 데이터 제공
 * - 복잡한 집계 쿼리 분리
 */
public interface BookStatisticsRepository {

    /**
     * 상태별 도서 개수
     */
    Map<String, Long> getBookCountByStatus();

    /**
     * 유형별 도서 개수
     */
    Map<String, Long> getBookCountByType();

    /**
     * 언어별 도서 개수
     */
    Map<String, Long> getBookCountByLanguage();

    /**
     * 전체 재고 수량
     */
    Long getTotalQuantity();

    /**
     * 이용 가능한 재고 수량
     */
    Long getAvailableQuantity();

    /**
     * 대출 중인 재고 수량
     */
    Long getBorrowedQuantity();
}