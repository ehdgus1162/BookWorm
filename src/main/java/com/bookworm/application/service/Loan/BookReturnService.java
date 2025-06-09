package com.bookworm.application.service.Loan;

import com.bookworm.application.dto.BookReturnRequest;
import com.bookworm.application.dto.BookReturnResponse;
import com.bookworm.application.dto.BookReturnStatistics;
import com.bookworm.application.dto.LoanResponse;
import com.bookworm.domain.common.TimeProvider;
import com.bookworm.domain.constant.LoanStatus;
import com.bookworm.domain.entity.BookLoan;
import com.bookworm.domain.exception.LoanBusinessException;
import com.bookworm.infrastructure.repository.BookLoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 도서 반납 서비스
 *
 * 핵심 설계 원리:
 * 1. 단일 책임: 도서 반납과 관련된 비즈니스 로직만 담당
 * 2. 트랜잭션 관리: 데이터 일관성 보장
 * 3. 도메인 로직 위임: 엔티티의 비즈니스 메서드 활용
 * 4. 예외 처리: 명확한 오류 메시지 제공
 * 5. 로깅: 추적 가능한 상세 로그
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookReturnService {

    private final BookLoanRepository bookLoanRepository;
    private final TimeProvider timeProvider;

    /**
     * 도서 반납 처리 (핵심 메서드)
     *
     * 처리 흐름:
     * 1. 대출 정보 조회 및 검증
     * 2. 반납 전 상태 정보 수집 (연체 여부, 연체 일수)
     * 3. 도메인 엔티티의 비즈니스 로직 실행
     * 4. 결과 응답 생성
     *
     * @param request 반납 요청 정보
     * @return 반납 처리 결과
     */
    public BookReturnResponse returnBook(BookReturnRequest request) {
        log.info("도서 반납 처리 시작. 대출 ID: {}", request.loanId());

        try {
            // 1. 활성 대출 조회
            BookLoan loan = findActiveLoan(request.loanId());

            // 2. 반납 전 상태 정보 수집 (중요: 반납 후에는 상태가 변경되므로 미리 수집)
            boolean wasOverdue = loan.isOverdue();
            long overdueDays = wasOverdue ? loan.getLoanPeriod().getOverdueDays() : 0;

            log.debug("반납 전 상태 정보 - 연체 여부: {}, 연체 일수: {}", wasOverdue, overdueDays);

            // 3. 도서 반납 실행 (핵심 비즈니스 로직은 엔티티가 담당)
            loan.returnBook(timeProvider);

            // 4. 변경사항 저장 (JPA dirty checking)
            bookLoanRepository.save(loan);

            log.info("도서 반납 완료. 대출 ID: {}, 연체 여부: {}, 연체 일수: {}",
                    loan.getId(), wasOverdue, overdueDays);

            // 5. 성공 응답 생성
            return createSuccessResponse(loan, wasOverdue, overdueDays);

        } catch (LoanBusinessException e) {
            log.warn("도서 반납 실패. 대출 ID: {}, 사유: {}", request.loanId(), e.getMessage());
            throw e; // 비즈니스 예외는 그대로 전파
        } catch (Exception e) {
            log.error("도서 반납 중 예기치 않은 오류 발생. 대출 ID: {}", request.loanId(), e);
            throw new LoanBusinessException("도서 반납 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 여러 도서 일괄 반납
     *
     * 원리: 개별 반납을 순차적으로 처리하되, 하나의 트랜잭션으로 묶어 원자성 보장
     * 하나라도 실패하면 전체 롤백
     */
    public List<BookReturnResponse> returnBooks(List<Long> loanIds) {
        log.info("도서 일괄 반납 처리 시작. 대출 건수: {}", loanIds.size());

        if (loanIds == null || loanIds.isEmpty()) {
            throw new LoanBusinessException("반납할 대출 목록이 비어있습니다.");
        }

        try {
            List<BookReturnResponse> responses = loanIds.stream()
                    .map(loanId -> returnBook(BookReturnRequest.of(loanId)))
                    .collect(Collectors.toList());

            log.info("도서 일괄 반납 완료. 성공 건수: {}", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("도서 일괄 반납 중 오류 발생. 대출 IDs: {}", loanIds, e);
            throw new LoanBusinessException("도서 일괄 반납 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 활성 상태의 대출 조회
     *
     * 원리: 조기 실패 패턴 (Fail Fast Pattern)
     * - 존재하지 않는 대출이나 이미 반납된 대출에 대해 즉시 명확한 오류 메시지 제공
     */
    private BookLoan findActiveLoan(Long loanId) {
        return bookLoanRepository.findByIdAndStatus(loanId, LoanStatus.ACTIVE)
                .orElseThrow(() -> new LoanBusinessException(
                        String.format("활성 상태의 대출을 찾을 수 없습니다. 대출 ID: %d", loanId)));
    }

    /**
     * 성공 응답 생성
     *
     * 원리: 데이터 변환과 캡슐화
     * - 엔티티의 내부 구조를 숨기고 클라이언트가 필요한 정보만 제공
     */
    private BookReturnResponse createSuccessResponse(BookLoan loan, boolean wasOverdue, long overdueDays) {
        return BookReturnResponse.success(
                loan.getId(),
                loan.getBook().getId(),
                loan.getBook().getTitle().getValue(),
                loan.getUser().getId(),
                loan.getUser().getFullName(),
                loan.getQuantity().getValue(),
                loan.getLoanPeriod().getLoanDate(),
                loan.getLoanPeriod().getDueDate(),
                timeProvider.currentDate(),
                wasOverdue,
                overdueDays
        );
    }

    // ===== 조회 관련 메서드들 =====

    /**
     * 사용자의 모든 활성 대출 조회
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> findUserActiveLoans(Long userId) {
        if (userId == null) {
            throw new LoanBusinessException("사용자 ID는 필수입니다.");
        }

        try {
            List<BookLoan> activeLoans = bookLoanRepository.findByUserIdAndStatus(userId, LoanStatus.ACTIVE);
            log.debug("사용자 활성 대출 조회: UserId={}, Count={}", userId, activeLoans.size());

            return activeLoans.stream()
                    .map(LoanResponse::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("사용자 활성 대출 조회 실패: UserId={}", userId, e);
            throw new LoanBusinessException("사용자 활성 대출 조회에 실패했습니다.", e);
        }
    }

    /**
     * 연체된 대출 목록 조회
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> findOverdueLoans() {
        try {
            List<BookLoan> overdueLoans = bookLoanRepository.findOverdueLoans(timeProvider.currentDate());
            log.debug("연체 대출 조회: Count={}", overdueLoans.size());

            if (!overdueLoans.isEmpty()) {
                log.warn("연체된 대출 발견: {} 건", overdueLoans.size());
            }

            return overdueLoans.stream()
                    .map(LoanResponse::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("연체 대출 조회 실패", e);
            throw new LoanBusinessException("연체 대출 조회에 실패했습니다.", e);
        }
    }

    /**
     * 곧 반납 예정인 대출 목록 조회 (알림용)
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> findUpcomingDueLoans(int days) {
        if (days <= 0) {
            throw new LoanBusinessException("조회할 일수는 1일 이상이어야 합니다.");
        }

        try {
            LocalDate today = timeProvider.currentDate();
            LocalDate futureDate = today.plusDays(days);

            List<BookLoan> upcomingLoans = bookLoanRepository.findUpcomingDueLoans(today, futureDate);
            log.debug("곧 반납 예정 대출 조회: Days={}, Count={}", days, upcomingLoans.size());

            return upcomingLoans.stream()
                    .map(LoanResponse::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("곧 반납 예정 대출 조회 실패: Days={}", days, e);
            throw new LoanBusinessException("곧 반납 예정 대출 조회에 실패했습니다.", e);
        }
    }

    /**
     * 특정 대출의 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public LoanResponse findLoanDetails(Long loanId) {
        if (loanId == null) {
            throw new LoanBusinessException("대출 ID는 필수입니다.");
        }

        try {
            BookLoan loan = bookLoanRepository.findById(loanId)
                    .orElseThrow(() -> new LoanBusinessException(
                            String.format("대출 정보를 찾을 수 없습니다. 대출 ID: %d", loanId)));

            log.debug("대출 상세 정보 조회: LoanId={}", loanId);
            return LoanResponse.from(loan);
        } catch (LoanBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("대출 상세 정보 조회 실패: LoanId={}", loanId, e);
            throw new LoanBusinessException("대출 상세 정보 조회에 실패했습니다.", e);
        }
    }

    /**
     * 반납 통계 조회
     */
    @Transactional(readOnly = true)
    public BookReturnStatistics getReturnStatistics(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new LoanBusinessException("통계 조회 기간은 필수입니다.");
        }

        if (startDate.isAfter(endDate)) {
            throw new LoanBusinessException("시작일은 종료일보다 늦을 수 없습니다.");
        }

        try {
            // 전체 반납 건수
            long totalReturns = bookLoanRepository.countLoansByPeriod(startDate, endDate);

            // 연체 반납 건수 (연체였던 대출들 중 해당 기간에 반납된 것들)
            List<BookLoan> returnedLoans = bookLoanRepository.findLoansBetweenDates(startDate, endDate)
                    .stream()
                    .filter(loan -> loan.getStatus() == LoanStatus.RETURNED)
                    .collect(Collectors.toList());

            long overdueReturns = returnedLoans.stream()
                    .mapToLong(loan -> loan.getLoanPeriod().getOverdueDays() > 0 ? 1 : 0)
                    .sum();

            log.debug("반납 통계 조회: Period={} to {}, Total={}, Overdue={}",
                    startDate, endDate, totalReturns, overdueReturns);

            return BookReturnStatistics.of(totalReturns, overdueReturns, timeProvider.currentDate());

        } catch (Exception e) {
            log.error("반납 통계 조회 실패: Period={} to {}", startDate, endDate, e);
            throw new LoanBusinessException("반납 통계 조회에 실패했습니다.", e);
        }
    }

    /**
     * 반납 가능한 대출 조회 (사용자별)
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> findReturnableLoans(Long userId) {
        if (userId == null) {
            throw new LoanBusinessException("사용자 ID는 필수입니다.");
        }

        try {
            List<BookLoan> returnableLoans = bookLoanRepository.findByUserIdAndStatus(userId, LoanStatus.ACTIVE);
            log.debug("반납 가능한 대출 조회: UserId={}, Count={}", userId, returnableLoans.size());

            return returnableLoans.stream()
                    .map(LoanResponse::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("반납 가능한 대출 조회 실패: UserId={}", userId, e);
            throw new LoanBusinessException("반납 가능한 대출 조회에 실패했습니다.", e);
        }
    }

    /**
     * 사용자의 반납 이력 조회
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> findUserReturnHistory(Long userId) {
        if (userId == null) {
            throw new LoanBusinessException("사용자 ID는 필수입니다.");
        }

        try {
            List<BookLoan> returnHistory = bookLoanRepository.findByUserId(userId)
                    .stream()
                    .filter(loan -> loan.getStatus() == LoanStatus.RETURNED)
                    .collect(Collectors.toList());

            log.debug("사용자 반납 이력 조회: UserId={}, Count={}", userId, returnHistory.size());

            return returnHistory.stream()
                    .map(LoanResponse::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("사용자 반납 이력 조회 실패: UserId={}", userId, e);
            throw new LoanBusinessException("사용자 반납 이력 조회에 실패했습니다.", e);
        }
    }
}