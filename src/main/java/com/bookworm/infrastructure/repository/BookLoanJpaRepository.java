package com.bookworm.infrastructure.repository;

import com.bookworm.domain.constant.LoanStatus;
import com.bookworm.domain.entity.BookLoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * BookLoan JPA Repository
 */
@Repository
public interface BookLoanJpaRepository extends JpaRepository<BookLoan, Long> {

    /**
     * 활성 대출 조회 (ID + 상태로)
     *
     * 원리: 복합 조건 쿼리로 정확한 대출만 조회
     */
    @Query("SELECT bl FROM BookLoan bl " +
            "JOIN FETCH bl.book b " +
            "JOIN FETCH bl.user u " +
            "WHERE bl.id = :id AND bl.status = :status")
    Optional<BookLoan> findByIdAndStatusWithFetch(@Param("id") Long id, @Param("status") LoanStatus status);

    /**
     * 사용자의 특정 상태 대출 조회
     */
    @Query("SELECT bl FROM BookLoan bl " +
            "JOIN FETCH bl.book b " +
            "JOIN FETCH bl.user u " +
            "WHERE bl.user.id = :userId AND bl.status = :status " +
            "ORDER BY bl.loanPeriod.dueDate ASC")
    List<BookLoan> findByUserIdAndStatusWithFetch(@Param("userId") Long userId, @Param("status") LoanStatus status);

    /**
     * 연체된 대출 조회
     *
     * 비즈니스 로직: 활성 상태이면서 반납예정일이 현재 날짜보다 이전인 대출
     */
    @Query("SELECT bl FROM BookLoan bl " +
            "JOIN FETCH bl.book b " +
            "JOIN FETCH bl.user u " +
            "WHERE bl.status = :status AND bl.loanPeriod.dueDate < :currentDate " +
            "ORDER BY bl.loanPeriod.dueDate ASC")
    List<BookLoan> findOverdueLoansWithFetch(@Param("status") LoanStatus status, @Param("currentDate") LocalDate currentDate);

    /**
     * 특정 기간 내 대출 건수
     */
    @Query("SELECT COUNT(bl) FROM BookLoan bl " +
            "WHERE bl.loanPeriod.loanDate BETWEEN :startDate AND :endDate")
    long countLoansByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 사용자별 대출 이력 (페이징, N+1 문제 해결)
     */
    @Query("SELECT bl FROM BookLoan bl " +
            "JOIN FETCH bl.book b " +
            "JOIN FETCH bl.user u " +
            "WHERE bl.user.id = :userId " +
            "ORDER BY bl.createdAt DESC")
    Page<BookLoan> findByUserIdWithFetch(@Param("userId") Long userId, Pageable pageable);

    /**
     * 도서별 대출 이력
     */
    @Query("SELECT bl FROM BookLoan bl " +
            "JOIN FETCH bl.book b " +
            "JOIN FETCH bl.user u " +
            "WHERE bl.book.id = :bookId " +
            "ORDER BY bl.createdAt DESC")
    List<BookLoan> findByBookIdWithFetch(@Param("bookId") Long bookId);

    /**
     * 곧 반납 예정인 대출 조회
     */
    @Query("SELECT bl FROM BookLoan bl " +
            "JOIN FETCH bl.book b " +
            "JOIN FETCH bl.user u " +
            "WHERE bl.status = 'ACTIVE' " +
            "AND bl.loanPeriod.dueDate BETWEEN :today AND :futureDate " +
            "ORDER BY bl.loanPeriod.dueDate ASC")
    List<BookLoan> findUpcomingDueLoansWithFetch(@Param("today") LocalDate today, @Param("futureDate") LocalDate futureDate);

    /**
     * 반납 완료된 대출 조회
     */
    @Query("SELECT bl FROM BookLoan bl " +
            "JOIN FETCH bl.book b " +
            "JOIN FETCH bl.user u " +
            "WHERE bl.status = 'RETURNED' " +
            "ORDER BY bl.updatedAt DESC")
    List<BookLoan> findReturnedLoansWithFetch();

    /**
     * 사용자의 활성 대출 수
     */
    @Query("SELECT COUNT(bl) FROM BookLoan bl " +
            "WHERE bl.user.id = :userId AND bl.status = 'ACTIVE'")
    long countActiveByUserId(@Param("userId") Long userId);
}