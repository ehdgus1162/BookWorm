package com.bookworm.infrastructure.repository;

import com.bookworm.domain.constant.LoanStatus;
import com.bookworm.domain.entity.BookLoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 도서 대출 레포지토리 인터페이스 (기존 인터페이스에 반납 관련 메서드만 추가)
 */
public interface BookLoanRepository {

    // ===== 기존 CRUD 메서드들 (유지) =====
    BookLoan save(BookLoan bookLoan);
    Optional<BookLoan> findById(Long id);
    Page<BookLoan> findAll(Pageable pageable);
    List<BookLoan> findAll();
    void delete(BookLoan bookLoan);
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();

    // ===== 기존 커스텀 조회 메서드들 (유지) =====
    List<BookLoan> findByUserId(Long userId);
    List<BookLoan> findActiveByUserId(Long userId);
    List<BookLoan> findByBookId(Long bookId);
    List<BookLoan> findActiveByBookId(Long bookId);
    List<BookLoan> findActiveLoans();
    List<BookLoan> findOverdueLoans(LocalDate currentDate);
    List<BookLoan> findReturnedLoans();
    List<BookLoan> findUpcomingDueLoans(LocalDate today, LocalDate futureDate);
    List<BookLoan> findLoansDueBetween(LocalDate startDate, LocalDate endDate);
    long countActiveByUserId(Long userId);
    long countByBookId(Long bookId);
    List<BookLoan> findLoansBetweenDates(LocalDate startDate, LocalDate endDate);
    List<BookLoan> findByUserNameContaining(String userName);
    List<BookLoan> findByBookTitleContaining(String bookTitle);

    // ===== 기존 동적 검색 메서드 (유지) =====
    Page<BookLoan> findLoansWithConditions(Long userId, Long bookId, String status,
                                           LocalDate loanDateFrom, LocalDate loanDateTo,
                                           LocalDate dueDateFrom, LocalDate dueDateTo,
                                           Boolean overdue, String userName, String bookTitle,
                                           Pageable pageable);

    // ===== 반납 관련 핵심 메서드 추가 =====

    /**
     * 활성 상태의 특정 대출 조회 (반납 처리용)
     */
    Optional<BookLoan> findByIdAndStatus(Long id, LoanStatus status);

    /**
     * 사용자의 특정 상태 대출 조회
     */
    List<BookLoan> findByUserIdAndStatus(Long userId, LoanStatus status);

    /**
     * 특정 기간 내 대출 통계 조회
     */
    long countLoansByPeriod(LocalDate startDate, LocalDate endDate);

    // ===== 기존 편의 메서드들 (유지) =====
    default List<BookLoan> findOverdueLoans() {
        return findOverdueLoans(LocalDate.now());
    }

    default List<BookLoan> findUpcomingDueLoans(int days) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);
        return findUpcomingDueLoans(today, futureDate);
    }
}
