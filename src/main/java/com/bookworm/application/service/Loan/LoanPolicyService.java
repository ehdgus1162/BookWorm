package com.bookworm.application.service.Loan;


import com.bookworm.domain.entity.Book;
import com.bookworm.domain.entity.BookLoan;
import com.bookworm.domain.entity.User;
import com.bookworm.infrastructure.repository.BookLoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 대출 정책 서비스
 * - 도서관 대출 정책 관리
 * - 정책 위반 검증
 * - 동적 정책 적용
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanPolicyService {

    private final BookLoanRepository bookLoanRepository;
    private final LoanAuditService loanAuditService;

    // 정책 상수들
    private static final int MAX_LOANS_PER_USER = 5;
    private static final int MAX_LOAN_DAYS = 30;
    private static final int DEFAULT_LOAN_DAYS = 14;
    private static final int MAX_EXTENSION_DAYS = 14;
    private static final int MAX_EXTENSIONS_PER_LOAN = 2;
    private static final int MAX_DAILY_LOANS_PER_USER = 3;

    /**
     * 사용자 대출 정책 검증
     */
    @Transactional(readOnly = true)
    public void validateUserLoanPolicy(User user, List<Book> books) {
        // 1. 사용자 상태 확인
        if (!user.isActive()) {
            throw new LoanPolicyViolationException("비활성화된 사용자는 도서를 대출할 수 없습니다.");
        }

        // 2. 최대 대출 권수 확인
        long currentActiveLoans = bookLoanRepository.countActiveByUserId(user.getId());
        if (currentActiveLoans + books.size() > MAX_LOANS_PER_USER) {
            loanAuditService.auditPolicyViolation(
                    user.getId(),
                    "MAX_LOANS_EXCEEDED",
                    String.format("현재 대출: %d권, 추가 요청: %d권, 최대 허용: %d권",
                            currentActiveLoans, books.size(), MAX_LOANS_PER_USER),
                    user.getEmail().getValue()
            );
            throw new LoanPolicyViolationException(
                    String.format("최대 %d권까지만 대출 가능합니다. 현재 대출 중: %d권",
                            MAX_LOANS_PER_USER, currentActiveLoans)
            );
        }

        // 3. 일일 대출 한도 확인
        validateDailyLoanLimit(user, books.size());

        // 4. 연체 대출 확인
        validateOverdueLoans(user);

        // 5. 블랙리스트 확인 (예: 연체가 자주 발생한 사용자)
        validateUserBlacklist(user);
    }

    /**
     * 도서 대출 정책 검증
     */
    public void validateBookLoanPolicy(Book book, int requestedQuantity) {
        // 1. 도서 상태 확인
        if (!book.isAvailable()) {
            throw new LoanPolicyViolationException(
                    String.format("도서 '%s'는 현재 대출할 수 없는 상태입니다.",
                            book.getTitle().getValue())
            );
        }

        // 2. 재고 확인
        if (!book.canBorrow(requestedQuantity)) {
            throw new LoanPolicyViolationException(
                    String.format("도서 '%s'의 재고가 부족합니다. 요청: %d권, 이용 가능: %d권",
                            book.getTitle().getValue(), requestedQuantity, book.getQuantity().getValue())
            );
        }

        // 3. 특수 도서 정책 확인 (예: 참고도서는 대출 불가)
        validateSpecialBookPolicy(book);
    }

    /**
     * 대출 기간 정책 검증
     */
    public void validateLoanPeriodPolicy(int loanDays) {
        if (loanDays > MAX_LOAN_DAYS) {
            throw new LoanPolicyViolationException(
                    String.format("최대 대출 기간은 %d일입니다. 요청: %d일", MAX_LOAN_DAYS, loanDays)
            );
        }

        if (loanDays <= 0) {
            throw new LoanPolicyViolationException("대출 기간은 1일 이상이어야 합니다.");
        }
    }

    /**
     * 대출 연장 정책 검증
     */
    @Transactional(readOnly = true)
    public void validateExtensionPolicy(BookLoan loan, int extensionDays) {
        // 1. 기본 연장 조건 확인
        if (!loan.isActive()) {
            throw new LoanPolicyViolationException("활성 상태가 아닌 대출은 연장할 수 없습니다.");
        }

        if (loan.isOverdue()) {
            throw new LoanPolicyViolationException("연체된 대출은 연장할 수 없습니다.");
        }

        // 2. 연장 일수 확인
        if (extensionDays > MAX_EXTENSION_DAYS) {
            throw new LoanPolicyViolationException(
                    String.format("최대 %d일까지만 연장 가능합니다.", MAX_EXTENSION_DAYS)
            );
        }

        // 3. 총 대출 기간 확인
        long totalDays = loan.getLoanPeriod().getTotalLoanDays() + extensionDays;
        if (totalDays > MAX_LOAN_DAYS) {
            throw new LoanPolicyViolationException(
                    String.format("총 대출 기간이 %d일을 초과할 수 없습니다.", MAX_LOAN_DAYS)
            );
        }

        // 4. 연장 횟수 확인 (실제로는 별도 테이블에서 관리)
        validateExtensionCount(loan);

        // 5. 다른 사용자의 예약 확인
        validateReservationConflict(loan);
    }

    /**
     * 일일 대출 한도 검증
     */
    @Transactional(readOnly = true)
    private void validateDailyLoanLimit(User user, int requestedBooks) {
        LocalDate today = LocalDate.now();
        List<BookLoan> todayLoans = bookLoanRepository.findLoansBetweenDates(today, today)
                .stream()
                .filter(loan -> loan.getUser().getId().equals(user.getId()))
                .toList();

        if (todayLoans.size() + requestedBooks > MAX_DAILY_LOANS_PER_USER) {
            loanAuditService.auditPolicyViolation(
                    user.getId(),
                    "DAILY_LOAN_LIMIT_EXCEEDED",
                    String.format("오늘 대출: %d권, 추가 요청: %d권, 일일 한도: %d권",
                            todayLoans.size(), requestedBooks, MAX_DAILY_LOANS_PER_USER),
                    user.getEmail().getValue()
            );
            throw new LoanPolicyViolationException(
                    String.format("하루에 최대 %d권까지만 대출 가능합니다. 오늘 대출: %d권",
                            MAX_DAILY_LOANS_PER_USER, todayLoans.size())
            );
        }
    }

    /**
     * 연체 대출 확인
     */
    @Transactional(readOnly = true)
    private void validateOverdueLoans(User user) {
        List<BookLoan> overdueLoans = bookLoanRepository.findActiveByUserId(user.getId())
                .stream()
                .filter(BookLoan::isOverdue)
                .toList();

        if (!overdueLoans.isEmpty()) {
            throw new LoanPolicyViolationException(
                    String.format("연체된 도서가 %d권 있습니다. 연체 도서를 먼저 반납해주세요.",
                            overdueLoans.size())
            );
        }
    }

    /**
     * 사용자 블랙리스트 확인
     */
    @Transactional(readOnly = true)
    private void validateUserBlacklist(User user) {
        // 최근 3개월간 연체 횟수 확인
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        long recentOverdueCount = bookLoanRepository.findByUserId(user.getId())
                .stream()
                .filter(loan -> loan.getLoanPeriod().getLoanDate().isAfter(threeMonthsAgo))
                .filter(loan -> loan.getLoanPeriod().getOverdueDays() > 0)
                .count();

        if (recentOverdueCount >= 5) { // 3개월간 5회 이상 연체
            loanAuditService.auditPolicyViolation(
                    user.getId(),
                    "FREQUENT_OVERDUE_USER",
                    String.format("최근 3개월간 연체 횟수: %d회", recentOverdueCount),
                    "SYSTEM"
            );
            throw new LoanPolicyViolationException(
                    "최근 연체가 자주 발생하여 일시적으로 대출이 제한됩니다. 관리자에게 문의하세요."
            );
        }
    }

    /**
     * 특수 도서 정책 확인
     */
    private void validateSpecialBookPolicy(Book book) {
        // 참고도서 (REFERENCE) 대출 금지
        if ("REFERENCE".equals(book.getType().getValue())) {
            throw new LoanPolicyViolationException(
                    String.format("참고도서 '%s'는 도서관 내에서만 이용 가능합니다.",
                            book.getTitle().getValue())
            );
        }

        // 신간 도서 (출간 후 1개월 이내) 대출 기간 제한
        // 실제로는 Book 엔티티에 출간일 필드가 있어야 함
    }

    /**
     * 연장 횟수 확인
     */
    private void validateExtensionCount(BookLoan loan) {
        // 실제로는 LoanExtensionHistory 엔티티에서 확인
        // 여기서는 간단히 구현
        int extensionCount = 0; // 실제로는 DB에서 조회

        if (extensionCount >= MAX_EXTENSIONS_PER_LOAN) {
            throw new LoanPolicyViolationException(
                    String.format("최대 %d회까지만 연장 가능합니다.", MAX_EXTENSIONS_PER_LOAN)
            );
        }
    }

    /**
     * 예약 충돌 확인
     */
    private void validateReservationConflict(BookLoan loan) {
        // 실제로는 BookReservation 엔티티에서 확인
        // 다른 사용자가 이 도서를 예약했다면 연장 불가
        boolean hasReservation = false; // 실제로는 DB에서 조회

        if (hasReservation) {
            throw new LoanPolicyViolationException(
                    "다른 사용자가 예약한 도서는 연장할 수 없습니다."
            );
        }
    }

}



