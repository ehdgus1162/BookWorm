package com.bookworm.application.service.Loan;

import com.bookworm.application.dto.BookReturnStatistics;
import com.bookworm.domain.common.TimeProvider;
import com.bookworm.infrastructure.repository.BookLoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 반납 통계 서비스
 *
 * 역할:
 * 1. 반납 관련 각종 통계 제공
 * 2. 대시보드용 메트릭 생성
 * 3. 리포트 데이터 제공
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class BookReturnStatisticsService {

    private final BookLoanRepository bookLoanRepository;
    private final TimeProvider timeProvider;

    /**
     * 오늘의 반납 통계
     */
    public BookReturnStatistics getTodayStatistics() {
        LocalDate today = timeProvider.currentDate();
        return getStatisticsForPeriod(today, today);
    }

    /**
     * 이번 주 반납 통계
     */
    public BookReturnStatistics getWeeklyStatistics() {
        LocalDate today = timeProvider.currentDate();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        return getStatisticsForPeriod(weekStart, today);
    }

    /**
     * 이번 달 반납 통계
     */
    public BookReturnStatistics getMonthlyStatistics() {
        LocalDate today = timeProvider.currentDate();
        LocalDate monthStart = today.withDayOfMonth(1);
        return getStatisticsForPeriod(monthStart, today);
    }

    /**
     * 특정 기간 반납 통계
     */
    public BookReturnStatistics getStatisticsForPeriod(LocalDate startDate, LocalDate endDate) {
        log.debug("반납 통계 조회: {} ~ {}", startDate, endDate);

        try {
            long totalReturns = bookLoanRepository.countLoansByPeriod(startDate, endDate);

            // 연체 반납 수는 별도 계산 필요 (실제로는 더 복잡한 쿼리 필요)
            long overdueReturns = calculateOverdueReturns(startDate, endDate);

            return BookReturnStatistics.of(totalReturns, overdueReturns, timeProvider.currentDate());

        } catch (Exception e) {
            log.error("반납 통계 조회 실패: {} ~ {}", startDate, endDate, e);
            throw new RuntimeException("반납 통계 조회에 실패했습니다.", e);
        }
    }

    /**
     * 대시보드용 종합 통계
     */
    public Map<String, Object> getDashboardStatistics() {
        log.debug("대시보드 통계 조회");

        try {
            Map<String, Object> stats = new HashMap<>();

            // 현재 활성 대출 수
            long activeLoans = bookLoanRepository.findActiveLoans().size();
            stats.put("activeLoans", activeLoans);

            // 현재 연체 대출 수
            long overdueLoans = bookLoanRepository.findOverdueLoans(timeProvider.currentDate()).size();
            stats.put("overdueLoans", overdueLoans);

            // 오늘 반납 예정 수
            long dueTodayLoans = bookLoanRepository.findUpcomingDueLoans(
                    timeProvider.currentDate(), timeProvider.currentDate()).size();
            stats.put("dueTodayLoans", dueTodayLoans);

            // 이번 주 반납 통계
            BookReturnStatistics weeklyStats = getWeeklyStatistics();
            stats.put("weeklyReturns", weeklyStats.totalReturns());
            stats.put("weeklyOverdueRate", weeklyStats.overdueRate());

            // 이번 달 반납 통계
            BookReturnStatistics monthlyStats = getMonthlyStatistics();
            stats.put("monthlyReturns", monthlyStats.totalReturns());
            stats.put("monthlyOverdueRate", monthlyStats.overdueRate());

            log.debug("대시보드 통계 조회 완료: {}", stats);
            return stats;

        } catch (Exception e) {
            log.error("대시보드 통계 조회 실패", e);
            throw new RuntimeException("대시보드 통계 조회에 실패했습니다.", e);
        }
    }

    /**
     * 사용자별 반납 통계
     */
    public Map<String, Object> getUserStatistics(Long userId) {
        log.debug("사용자 반납 통계 조회: UserId={}", userId);

        try {
            Map<String, Object> stats = new HashMap<>();

            // 사용자의 총 대출 수
            long totalLoans = bookLoanRepository.findByUserId(userId).size();
            stats.put("totalLoans", totalLoans);

            // 사용자의 현재 활성 대출 수
            long activeLoans = bookLoanRepository.countActiveByUserId(userId);
            stats.put("activeLoans", activeLoans);

            // 사용자의 반납 완료 수
            long returnedLoans = totalLoans - activeLoans;
            stats.put("returnedLoans", returnedLoans);

            // 반납률 계산
            double returnRate = totalLoans > 0 ? (double) returnedLoans / totalLoans * 100 : 0.0;
            stats.put("returnRate", returnRate);

            log.debug("사용자 반납 통계 조회 완료: UserId={}, Stats={}", userId, stats);
            return stats;

        } catch (Exception e) {
            log.error("사용자 반납 통계 조회 실패: UserId={}", userId, e);
            throw new RuntimeException("사용자 반납 통계 조회에 실패했습니다.", e);
        }
    }

    /**
     * 연체 반납 수 계산 (헬퍼 메서드)
     */
    private long calculateOverdueReturns(LocalDate startDate, LocalDate endDate) {
        // 실제 구현에서는 더 복잡한 쿼리가 필요할 수 있음
        // 여기서는 간단한 계산으로 대체
        return bookLoanRepository.findLoansBetweenDates(startDate, endDate)
                .stream()
                .mapToLong(loan -> loan.getLoanPeriod().getOverdueDays() > 0 ? 1 : 0)
                .sum();
    }
}
