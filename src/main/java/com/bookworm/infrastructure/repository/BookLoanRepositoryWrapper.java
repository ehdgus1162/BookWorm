package com.bookworm.infrastructure.repository;


// 이 줄을 추가하세요!
import com.bookworm.infrastructure.repository.BookLoanJpaRepository;

import com.bookworm.domain.constant.LoanStatus;
import com.bookworm.domain.entity.BookLoan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * BookLoanRepository 통합 구현체 (Import 문제 해결)
 *
 * 설계 원리:
 * 1. 기존 QueryDSL 구현체와 JPA Repository를 조합
 * 2. 반납 관련 메서드는 JPA의 Fetch Join 쿼리 활용
 * 3. 복잡한 동적 쿼리는 QueryDSL 구현체 활용
 * 4. 모든 메서드를 완전히 구현하여 인터페이스 준수
 */
@RequiredArgsConstructor
@Transactional
@Slf4j
@Repository
public class BookLoanRepositoryWrapper implements BookLoanRepository {

    // 명시적으로 인터페이스 타입으로 주입받기
    private final BookLoanJpaRepository bookLoanJpaRepository;


    private final BookLoanRepositoryImpl bookLoanRepositoryImpl;

    // ===== 기본 CRUD 메서드들 =====

    @Override
    public BookLoan save(BookLoan bookLoan) {
        if (bookLoan == null) {
            throw new IllegalArgumentException("BookLoan 객체는 필수입니다.");
        }

        try {
            BookLoan saved = bookLoanJpaRepository.save(bookLoan);
            log.debug("대출 저장 완료: ID={}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("대출 저장 실패", e);
            throw new RuntimeException("대출 저장에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookLoan> findById(Long id) {
        if (id == null) {
            log.warn("대출 조회 시도 - ID가 null입니다.");
            return Optional.empty();
        }

        try {
            return bookLoanJpaRepository.findById(id);
        } catch (Exception e) {
            log.error("대출 조회 실패: ID={}", id, e);
            throw new RuntimeException("대출 조회에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookLoan> findAll(Pageable pageable) {
        try {
            return bookLoanJpaRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("대출 목록 조회 실패", e);
            throw new RuntimeException("대출 목록 조회에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookLoan> findAll() {
        try {
            return bookLoanJpaRepository.findAll();
        } catch (Exception e) {
            log.error("전체 대출 목록 조회 실패", e);
            throw new RuntimeException("전체 대출 목록 조회에 실패했습니다.", e);
        }
    }

    @Override
    public void delete(BookLoan bookLoan) {
        if (bookLoan == null) {
            log.warn("대출 삭제 시도 - 객체가 null입니다.");
            return;
        }

        try {
            bookLoanJpaRepository.delete(bookLoan);
            log.info("대출 삭제 완료: ID={}", bookLoan.getId());
        } catch (Exception e) {
            log.error("대출 삭제 실패: ID={}", bookLoan.getId(), e);
            throw new RuntimeException("대출 삭제에 실패했습니다.", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            log.warn("대출 삭제 시도 - ID가 null입니다.");
            return;
        }

        try {
            bookLoanJpaRepository.deleteById(id);
            log.info("대출 삭제 완료: ID={}", id);
        } catch (Exception e) {
            log.error("대출 삭제 실패: ID={}", id, e);
            throw new RuntimeException("대출 삭제에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }

        try {
            return bookLoanJpaRepository.existsById(id);
        } catch (Exception e) {
            log.error("대출 존재 여부 확인 실패: ID={}", id, e);
            throw new RuntimeException("대출 존재 여부 확인에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        try {
            return bookLoanJpaRepository.count();
        } catch (Exception e) {
            log.error("전체 대출 수 조회 실패", e);
            throw new RuntimeException("전체 대출 수 조회에 실패했습니다.", e);
        }
    }

    // ===== 반납 관련 핵심 메서드들 (JPA Repository 활용) =====

    @Override
    @Transactional(readOnly = true)
    public Optional<BookLoan> findByIdAndStatus(Long id, LoanStatus status) {
        if (id == null || status == null) {
            log.warn("대출 상태별 조회 시도 - ID 또는 상태가 null입니다. ID={}, Status={}", id, status);
            return Optional.empty();
        }

        try {
            Optional<BookLoan> result = bookLoanJpaRepository.findByIdAndStatusWithFetch(id, status);
            log.debug("대출 상태별 조회: ID={}, Status={}, Found={}", id, status, result.isPresent());
            return result;
        } catch (Exception e) {
            log.error("대출 상태별 조회 실패: ID={}, Status={}", id, status, e);
            throw new RuntimeException("대출 상태별 조회에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookLoan> findByUserIdAndStatus(Long userId, LoanStatus status) {
        if (userId == null || status == null) {
            log.warn("사용자별 대출 조회 시도 - UserId 또는 상태가 null입니다. UserId={}, Status={}", userId, status);
            return List.of();
        }

        try {
            List<BookLoan> loans = bookLoanJpaRepository.findByUserIdAndStatusWithFetch(userId, status);
            log.debug("사용자별 대출 조회: UserId={}, Status={}, Count={}", userId, status, loans.size());
            return loans;
        } catch (Exception e) {
            log.error("사용자별 대출 조회 실패: UserId={}, Status={}", userId, status, e);
            throw new RuntimeException("사용자별 대출 조회에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countLoansByPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            log.warn("기간별 대출 수 조회 시도 - 날짜가 null입니다. Start={}, End={}", startDate, endDate);
            return 0;
        }

        if (startDate.isAfter(endDate)) {
            log.warn("기간별 대출 수 조회 시도 - 시작일이 종료일보다 늦습니다. Start={}, End={}", startDate, endDate);
            return 0;
        }

        try {
            long count = bookLoanJpaRepository.countLoansByPeriod(startDate, endDate);
            log.debug("기간별 대출 수 조회: Start={}, End={}, Count={}", startDate, endDate, count);
            return count;
        } catch (Exception e) {
            log.error("기간별 대출 수 조회 실패: Start={}, End={}", startDate, endDate, e);
            throw new RuntimeException("기간별 대출 수 조회에 실패했습니다.", e);
        }
    }

    // ===== 기존 QueryDSL 구현체로 위임하는 메서드들 =====

    @Override
    public List<BookLoan> findByUserId(Long userId) {
        return bookLoanRepositoryImpl.findByUserId(userId);
    }

    @Override
    public List<BookLoan> findActiveByUserId(Long userId) {
        return bookLoanRepositoryImpl.findActiveByUserId(userId);
    }

    @Override
    public List<BookLoan> findByBookId(Long bookId) {
        return bookLoanRepositoryImpl.findByBookId(bookId);
    }

    @Override
    public List<BookLoan> findActiveByBookId(Long bookId) {
        return bookLoanRepositoryImpl.findActiveByBookId(bookId);
    }

    @Override
    public List<BookLoan> findActiveLoans() {
        return bookLoanRepositoryImpl.findActiveLoans();
    }

    @Override
    public List<BookLoan> findOverdueLoans(LocalDate currentDate) {
        return bookLoanRepositoryImpl.findOverdueLoans(currentDate);
    }

    @Override
    public List<BookLoan> findReturnedLoans() {
        return bookLoanRepositoryImpl.findReturnedLoans();
    }

    @Override
    public List<BookLoan> findUpcomingDueLoans(LocalDate today, LocalDate futureDate) {
        return bookLoanRepositoryImpl.findUpcomingDueLoans(today, futureDate);
    }

    @Override
    public List<BookLoan> findLoansDueBetween(LocalDate startDate, LocalDate endDate) {
        return bookLoanRepositoryImpl.findLoansDueBetween(startDate, endDate);
    }

    @Override
    public long countActiveByUserId(Long userId) {
        return bookLoanRepositoryImpl.countActiveByUserId(userId);
    }

    @Override
    public long countByBookId(Long bookId) {
        return bookLoanRepositoryImpl.countByBookId(bookId);
    }

    @Override
    public List<BookLoan> findLoansBetweenDates(LocalDate startDate, LocalDate endDate) {
        return bookLoanRepositoryImpl.findLoansBetweenDates(startDate, endDate);
    }

    @Override
    public List<BookLoan> findByUserNameContaining(String userName) {
        return bookLoanRepositoryImpl.findByUserNameContaining(userName);
    }

    @Override
    public List<BookLoan> findByBookTitleContaining(String bookTitle) {
        return bookLoanRepositoryImpl.findByBookTitleContaining(bookTitle);
    }

    @Override
    public Page<BookLoan> findLoansWithConditions(Long userId, Long bookId, String status,
                                                  LocalDate loanDateFrom, LocalDate loanDateTo,
                                                  LocalDate dueDateFrom, LocalDate dueDateTo,
                                                  Boolean overdue, String userName, String bookTitle,
                                                  Pageable pageable) {
        return bookLoanRepositoryImpl.findLoansWithConditions(
                userId, bookId, status, loanDateFrom, loanDateTo,
                dueDateFrom, dueDateTo, overdue, userName, bookTitle, pageable
        );
    }
}
