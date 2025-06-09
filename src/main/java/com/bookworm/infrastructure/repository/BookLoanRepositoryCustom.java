package com.bookworm.infrastructure.repository;

import com.bookworm.domain.entity.BookLoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * BookLoan Repository의 커스텀 인터페이스
 * QueryDSL을 사용한 동적 쿼리 정의
 */
@Repository
public interface BookLoanRepositoryCustom {

    /**
     * 특정 사용자의 모든 대출 조회
     */
    List<BookLoan> findByUserId(Long userId);

    /**
     * 특정 사용자의 활성 대출 조회
     */
    List<BookLoan> findActiveByUserId(Long userId);

    /**
     * 특정 도서의 모든 대출 이력 조회
     */
    List<BookLoan> findByBookId(Long bookId);

    /**
     * 특정 도서의 활성 대출 조회
     */
    List<BookLoan> findActiveByBookId(Long bookId);

    /**
     * 모든 활성 대출 조회
     */
    List<BookLoan> findActiveLoans();

    /**
     * 연체된 대출 조회
     */
    List<BookLoan> findOverdueLoans(LocalDate currentDate);

    /**
     * 연체된 대출 조회 (현재 날짜 기준)
     */
    default List<BookLoan> findOverdueLoans() {
        return findOverdueLoans(LocalDate.now());
    }

    /**
     * 반납 완료된 대출 조회
     */
    List<BookLoan> findReturnedLoans();

    /**
     * 곧 반납 예정인 대출 조회
     */
    List<BookLoan> findUpcomingDueLoans(LocalDate today, LocalDate futureDate);

    /**
     * 곧 반납 예정인 대출 조회 (일수 지정)
     */
    default List<BookLoan> findUpcomingDueLoans(int days) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);
        return findUpcomingDueLoans(today, futureDate);
    }

    /**
     * 반납 예정일이 특정 기간 내인 대출 조회
     */
    List<BookLoan> findLoansDueBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 특정 사용자의 활성 대출 수 조회
     */
    long countActiveByUserId(Long userId);

    /**
     * 특정 도서의 총 대출 횟수 조회
     */
    long countByBookId(Long bookId);

    /**
     * 특정 기간 내 대출 조회
     */
    List<BookLoan> findLoansBetweenDates(LocalDate startDate, LocalDate endDate);

    /**
     * 사용자 이름으로 대출 검색
     */
    List<BookLoan> findByUserNameContaining(String userName);

    /**
     * 도서 제목으로 대출 검색
     */
    List<BookLoan> findByBookTitleContaining(String bookTitle);

    /**
     * 동적 검색 (복합 조건)
     */
    Page<BookLoan> findLoansWithConditions(Long userId, Long bookId, String status,
                                           LocalDate loanDateFrom, LocalDate loanDateTo,
                                           LocalDate dueDateFrom, LocalDate dueDateTo,
                                           Boolean overdue, String userName, String bookTitle,
                                           Pageable pageable);
}