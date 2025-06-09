package com.bookworm.infrastructure.repository;


import com.bookworm.domain.repository.BookStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 도서 통계 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class BookStatisticsRepositoryImpl implements BookStatisticsRepository {


    private final BookJpaRepository bookJpaRepository;

    @Override
    public Map<String, Long> getBookCountByStatus() {
        try {
            List<Object[]> results = bookJpaRepository.countByStatus();
            Map<String, Long> statusCounts = new HashMap<>();

            for (Object[] result : results) {
                String status = result[0].toString();
                Long count = (Long) result[1];
                statusCounts.put(status, count);
            }

            return statusCounts;
        } catch (Exception e) {
            log.error("상태별 도서 개수 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("상태별 도서 개수 조회에 실패했습니다.", e);
        }
    }

    @Override
    public Map<String, Long> getBookCountByType() {
        try {
            List<Object[]> results = bookJpaRepository.countByType();
            return getStringLongMap(results);
        } catch (Exception e) {
            log.error("유형별 도서 개수 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("유형별 도서 개수 조회에 실패했습니다.", e);
        }
    }

    @Override
    public Map<String, Long> getBookCountByLanguage() {
        try {
            List<Object[]> results = bookJpaRepository.countByLanguage();
            return getStringLongMap(results);
        } catch (Exception e) {
            log.error("언어별 도서 개수 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("언어별 도서 개수 조회에 실패했습니다.", e);
        }
    }

    private Map<String, Long> getStringLongMap(List<Object[]> results) {
        Map<String, Long> languageCounts = new HashMap<>();

        for (Object[] result : results) {
            String language = (String) result[0];
            Long count = (Long) result[1];
            languageCounts.put(language, count);
        }

        return languageCounts;
    }

    @Override
    public Long getTotalQuantity() {
        try {
            Long total = bookJpaRepository.sumTotalQuantity();
            return total != null ? total : 0L;
        } catch (Exception e) {
            log.error("전체 재고 수량 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("전체 재고 수량 조회에 실패했습니다.", e);
        }
    }

    @Override
    public Long getAvailableQuantity() {
        try {
            Long available = bookJpaRepository.sumAvailableQuantity();
            return available != null ? available : 0L;
        } catch (Exception e) {
            log.error("이용 가능한 재고 수량 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이용 가능한 재고 수량 조회에 실패했습니다.", e);
        }
    }

    public Long getBorrowedQuantity() {
        try {
            Long total = getTotalQuantity();
            Long available = getAvailableQuantity();
            return total - available;
        } catch (Exception e) {
            log.error("대출 중인 재고 수량 계산 실패: {}", e.getMessage(), e);
            throw new RuntimeException("대출 중인 재고 수량 계산에 실패했습니다.", e);
        }
    }
}
