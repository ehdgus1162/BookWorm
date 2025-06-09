package com.bookworm.application.service.Loan;

import com.bookworm.application.dto.LoanAuditReportDto;
import com.bookworm.domain.entity.BookLoan;

import com.bookworm.infrastructure.repository.BookLoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 대출 감사 서비스
 * - 대출 활동 추적
 * - 감사 로그 생성
 * - 규정 준수 확인
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanAuditService {

    private final BookLoanRepository bookLoanRepository;

    /**
     * 대출 생성 감사 로그
     */
    public void auditLoanCreation(BookLoan loan, String performedBy) {
        try {
            Map<String, Object> auditData = Map.of(
                    "eventType", "LOAN_CREATED",
                    "loanId", loan.getId(),
                    "userId", loan.getUser().getId(),
                    "userName", loan.getUser().getFullName(),
                    "bookId", loan.getBook().getId(),
                    "bookTitle", loan.getBook().getTitle().getValue(),
                    "quantity", loan.getQuantity().getValue(),
                    "dueDate", loan.getLoanPeriod().getDueDate(),
                    "performedBy", performedBy,
                    "timestamp", java.time.LocalDateTime.now()
            );

            // 실제로는 별도의 감사 로그 저장소에 저장
            log.info("대출 생성 감사 로그: {}", auditData);

        } catch (Exception e) {
            log.error("대출 생성 감사 로그 기록 실패 - 대출 ID: {}", loan.getId(), e);
        }
    }

    /**
     * 반납 감사 로그
     */
    public void auditBookReturn(BookLoan loan, String performedBy) {
        try {
            Map<String, Object> auditData = Map.of(
                    "eventType", "BOOK_RETURNED",
                    "loanId", loan.getId(),
                    "userId", loan.getUser().getId(),
                    "userName", loan.getUser().getFullName(),
                    "bookId", loan.getBook().getId(),
                    "bookTitle", loan.getBook().getTitle().getValue(),
                    "returnDate", LocalDate.now(),
                    "wasOverdue", loan.isOverdue(),
                    "performedBy", performedBy,
                    "timestamp", java.time.LocalDateTime.now()
            );

            log.info("반납 감사 로그: {}", auditData);

        } catch (Exception e) {
            log.error("반납 감사 로그 기록 실패 - 대출 ID: {}", loan.getId(), e);
        }
    }

    /**
     * 대출 연장 감사 로그
     */
    public void auditLoanExtension(BookLoan loan, int extensionDays, String performedBy) {
        try {
            Map<String, Object> auditData = Map.of(
                    "eventType", "LOAN_EXTENDED",
                    "loanId", loan.getId(),
                    "userId", loan.getUser().getId(),
                    "userName", loan.getUser().getFullName(),
                    "bookId", loan.getBook().getId(),
                    "bookTitle", loan.getBook().getTitle().getValue(),
                    "extensionDays", extensionDays,
                    "newDueDate", loan.getLoanPeriod().getDueDate(),
                    "performedBy", performedBy,
                    "timestamp", java.time.LocalDateTime.now()
            );

            log.info("대출 연장 감사 로그: {}", auditData);

        } catch (Exception e) {
            log.error("대출 연장 감사 로그 기록 실패 - 대출 ID: {}", loan.getId(), e);
        }
    }

    /**
     * 대출 취소 감사 로그
     */
    public void auditLoanCancellation(BookLoan loan, String performedBy, String reason) {
        try {
            Map<String, Object> auditData = Map.of(
                    "eventType", "LOAN_CANCELLED",
                    "loanId", loan.getId(),
                    "userId", loan.getUser().getId(),
                    "userName", loan.getUser().getFullName(),
                    "bookId", loan.getBook().getId(),
                    "bookTitle", loan.getBook().getTitle().getValue(),
                    "cancellationDate", LocalDate.now(),
                    "reason", reason != null ? reason : "사용자 요청",
                    "performedBy", performedBy,
                    "timestamp", java.time.LocalDateTime.now()
            );

            log.info("대출 취소 감사 로그: {}", auditData);

        } catch (Exception e) {
            log.error("대출 취소 감사 로그 기록 실패 - 대출 ID: {}", loan.getId(), e);
        }
    }

    /**
     * 연체 발생 감사 로그
     */
    @Transactional(readOnly = true)
    public void auditOverdueDetection() {
        try {
            List<BookLoan> overdueLoans = bookLoanRepository.findOverdueLoans();

            for (BookLoan loan : overdueLoans) {
                Map<String, Object> auditData = Map.of(
                        "eventType", "OVERDUE_DETECTED",
                        "loanId", loan.getId(),
                        "userId", loan.getUser().getId(),
                        "userName", loan.getUser().getFullName(),
                        "bookId", loan.getBook().getId(),
                        "bookTitle", loan.getBook().getTitle().getValue(),
                        "dueDate", loan.getLoanPeriod().getDueDate(),
                        "overdueDays", loan.getLoanPeriod().getOverdueDays(),
                        "performedBy", "SYSTEM",
                        "timestamp", java.time.LocalDateTime.now()
                );

                log.info("연체 감지 감사 로그: {}", auditData);
            }

            if (!overdueLoans.isEmpty()) {
                log.info("연체 감지 감사 로그 기록 완료 - 연체 대출 건수: {}", overdueLoans.size());
            }

        } catch (Exception e) {
            log.error("연체 감지 감사 로그 기록 실패", e);
        }
    }

    /**
     * 대출 정책 위반 감사 로그
     */
    public void auditPolicyViolation(Long userId, String violationType, String details, String performedBy) {
        try {
            Map<String, Object> auditData = Map.of(
                    "eventType", "POLICY_VIOLATION",
                    "userId", userId,
                    "violationType", violationType,
                    "violationDetails", details,
                    "detectionDate", LocalDate.now(),
                    "performedBy", performedBy,
                    "timestamp", java.time.LocalDateTime.now()
            );

            log.warn("대출 정책 위반 감사 로그: {}", auditData);

        } catch (Exception e) {
            log.error("대출 정책 위반 감사 로그 기록 실패 - 사용자 ID: {}", userId, e);
        }
    }

    /**
     * 대량 대출 활동 감사 (의심스러운 활동 탐지)
     */
    @Transactional(readOnly = true)
    public void auditSuspiciousLoanActivity() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(1); // 하루 동안의 활동 확인

            List<BookLoan> recentLoans = bookLoanRepository.findLoansBetweenDates(startDate, today);

            // 사용자별 대출 수 집계
            Map<Long, Long> userLoanCounts = recentLoans.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            loan -> loan.getUser().getId(),
                            java.util.stream.Collectors.counting()
                    ));

            // 의심스러운 활동 탐지 (하루에 5권 이상 대출)
            userLoanCounts.entrySet().stream()
                    .filter(entry -> entry.getValue() >= 5)
                    .forEach(entry -> {
                        Long userId = entry.getKey();
                        Long loanCount = entry.getValue();

                        auditPolicyViolation(
                                userId,
                                "EXCESSIVE_DAILY_LOANS",
                                String.format("하루에 %d권 대출", loanCount),
                                "SYSTEM"
                        );
                    });

            log.info("의심스러운 대출 활동 감사 완료 - 검사 대상: {} 건", recentLoans.size());

        } catch (Exception e) {
            log.error("의심스러운 대출 활동 감사 실패", e);
        }
    }

    /**
     * 대출 통계 감사 리포트 생성
     */
    @Transactional(readOnly = true)
    public LoanAuditReportDto generateAuditReport(LocalDate startDate, LocalDate endDate) {
        try {
            List<BookLoan> loansInPeriod = bookLoanRepository.findLoansBetweenDates(startDate, endDate);

            long totalLoans = loansInPeriod.size();
            long overdueLoans = loansInPeriod.stream()
                    .mapToLong(loan -> loan.isOverdue() ? 1 : 0)
                    .sum();
            long returnedLoans = loansInPeriod.stream()
                    .mapToLong(loan -> loan.isReturned() ? 1 : 0)
                    .sum();

            double overdueRate = totalLoans > 0 ? (double) overdueLoans / totalLoans * 100 : 0;

            Map<String, Object> reportData = Map.of(
                    "eventType", "AUDIT_REPORT_GENERATED",
                    "reportPeriodStart", startDate,
                    "reportPeriodEnd", endDate,
                    "totalLoans", totalLoans,
                    "overdueLoans", overdueLoans,
                    "returnedLoans", returnedLoans,
                    "overdueRate", overdueRate,
                    "performedBy", "SYSTEM",
                    "timestamp", java.time.LocalDateTime.now()
            );

            log.info("대출 감사 리포트 생성: {}", reportData);

            log.info("대출 감사 리포트 생성 완료 - 기간: {} ~ {}, 총 대출: {} 건",
                    startDate, endDate, totalLoans);

            return new LoanAuditReportDto(
                    startDate,
                    endDate,
                    totalLoans,
                    overdueLoans,
                    returnedLoans,
                    overdueRate
            );

        } catch (Exception e) {
            log.error("대출 감사 리포트 생성 실패 - 기간: {} ~ {}", startDate, endDate, e);
            throw new RuntimeException("감사 리포트 생성에 실패했습니다.", e);
        }
    }
}