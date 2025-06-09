package com.bookworm.application.service.Loan;

import com.bookworm.application.dto.*;
import com.bookworm.domain.entity.Book;
import com.bookworm.domain.entity.BookLoan;
import com.bookworm.domain.entity.User;
import com.bookworm.domain.exception.LoanBusinessException;
import com.bookworm.infrastructure.repository.BookLoanRepository;
import com.bookworm.domain.vo.bookloan.LoanPeriod;
import com.bookworm.domain.vo.bookloan.LoanQuantity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 간단한 대출 서비스
 * - 핵심 기능만 구현
 * - 복잡한 검증과 후처리 최소화
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final BookLoanRepository bookLoanRepository;
    private final LoanHelper loanHelper;
    private final LoanMapper loanMapper;

    // ==================== 대출 생성 ====================

    /**
     * 여러 도서 동시 대출
     */
    @Transactional
    public BorrowResponseDto borrowBooks(BorrowRequestDto request) {
        log.info("도서 대출 요청 - 사용자 ID: {}, 도서 수: {}",
                request.userId(), request.bookIds().size());

        // 기본 검증
        validateBorrowRequest(request);

        // 엔티티 조회
        User user = loanHelper.getUserOrThrow(request.userId());
        List<Book> books = loanHelper.getBooksOrThrow(request.bookIds());

        // 대출 가능성 확인
        validateBorrowEligibility(user, books);

        // 대출 생성
        LoanPeriod loanPeriod = request.dueDate() != null
                ? LoanPeriod.of(LocalDate.now(), request.dueDate())
                : LoanPeriod.createDefault();

        List<BookLoan> loans = books.stream()
                .map(book -> createAndExecuteLoan(book, user, loanPeriod))
                .toList();

        // 저장
        // 저장
        List<BookLoan> savedLoans = loans.stream()
                .map(bookLoanRepository::save)
                .toList();

        log.info("도서 대출 완료 - 사용자: {}, 대출 도서 수: {}", user.getFullName(), books.size());

        return loanMapper.toBorrowResponseDto(user, books, savedLoans.get(0), loanPeriod);
    }

    /**
     * 단일 도서 대출
     */
    @Transactional
    public LoanResponse borrowSingleBook(SingleLoanRequest request) {
        log.info("단일 도서 대출 요청 - 도서 ID: {}, 사용자 ID: {}",
                request.bookId(), request.userId());

        User user = loanHelper.getUserOrThrow(request.userId());
        Book book = loanHelper.getBookOrThrow(request.bookId());

        // 간단한 검증
        if (!user.isActive()) {
            throw new LoanBusinessException("비활성화된 사용자는 도서를 대출할 수 없습니다.");
        }
        if (!book.canBorrow(1)) {
            throw new LoanBusinessException("해당 도서는 현재 대출할 수 없습니다.");
        }

        LoanPeriod loanPeriod = request.loanDays() != null
                ? LoanPeriod.ofDays(request.loanDays())
                : LoanPeriod.createDefault();

        BookLoan loan = createAndExecuteLoan(book, user, loanPeriod);
        BookLoan savedLoan = bookLoanRepository.save(loan);

        log.info("단일 도서 대출 완료 - 사용자: {}, 도서: '{}'",
                user.getFullName(), book.getTitle().getValue());

        return LoanResponse.from(savedLoan);
    }

    // ==================== 대출 관리 ====================

    /**
     * 도서 반납
     */
    @Transactional
    public LoanResponse returnBook(Long loanId) {
        log.info("도서 반납 요청 - 대출 ID: {}", loanId);

        BookLoan loan = loanHelper.getLoanOrThrow(loanId);

        if (!loan.isActive()) {
            throw new LoanBusinessException("활성 상태가 아닌 대출은 반납할 수 없습니다.");
        }

        loan.returnBook(null); // TimeProvider 없이 간단하게
        BookLoan updatedLoan = bookLoanRepository.save(loan);

        log.info("도서 반납 완료 - 대출 ID: {}", loanId);
        return LoanResponse.from(updatedLoan);
    }

    /**
     * 대출 연장
     */
    @Transactional
    public LoanResponse extendLoan(Long loanId, ExtendLoanRequest request) {
        log.info("대출 연장 요청 - 대출 ID: {}, 연장 일수: {}일", loanId, request.extensionDays());

        BookLoan loan = loanHelper.getLoanOrThrow(loanId);

        if (!loan.isActive()) {
            throw new LoanBusinessException("활성 상태가 아닌 대출은 연장할 수 없습니다.");
        }
        if (loan.isOverdue()) {
            throw new LoanBusinessException("연체된 대출은 연장할 수 없습니다.");
        }
        if (request.extensionDays() <= 0 || request.extensionDays() > 14) {
            throw new LoanBusinessException("연장 일수는 1일 이상 14일 이하여야 합니다.");
        }

        loan.extendLoan(request.extensionDays());
        BookLoan updatedLoan = bookLoanRepository.save(loan);

        log.info("대출 연장 완료 - 대출 ID: {}, 새 반납일: {}", loanId, loan.getLoanPeriod().getDueDate());
        return LoanResponse.from(updatedLoan);
    }

    // ==================== 대출 조회 ====================

    /**
     * 대출 상세 조회
     */
    @Transactional(readOnly = true)
    public LoanResponse getLoan(Long loanId) {
        BookLoan loan = loanHelper.getLoanOrThrow(loanId);
        return LoanResponse.from(loan);
    }

    /**
     * 모든 대출 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<LoanResponse> getAllLoans(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookLoan> loanPage = bookLoanRepository.findAll(pageable);
        return loanPage.map(LoanResponse::from);
    }

    /**
     * 사용자별 대출 조회
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> getUserLoans(Long userId) {
        List<BookLoan> loans = bookLoanRepository.findByUserId(userId);
        return loans.stream().map(LoanResponse::from).toList();
    }

    /**
     * 활성 대출 조회
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> getActiveLoans() {
        List<BookLoan> activeLoans = bookLoanRepository.findActiveLoans();
        return activeLoans.stream().map(LoanResponse::from).toList();
    }

    /**
     * 연체 대출 조회
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> getOverdueLoans() {
        List<BookLoan> overdueLoans = bookLoanRepository.findOverdueLoans();
        return overdueLoans.stream().map(LoanResponse::from).toList();
    }

    // ==================== Private 메서드 ====================

    /**
     * 대출 요청 기본 검증
     */
    private void validateBorrowRequest(BorrowRequestDto request) {
        if (request.userId() == null) {
            throw new LoanBusinessException("사용자 ID는 필수입니다.");
        }
        if (request.bookIds() == null || request.bookIds().isEmpty()) {
            throw new LoanBusinessException("대출할 도서를 선택해주세요.");
        }
        if (request.bookIds().size() > 5) {
            throw new LoanBusinessException("한 번에 최대 5권까지만 대출 가능합니다.");
        }
    }

    /**
     * 대출 자격 확인
     */
    private void validateBorrowEligibility(User user, List<Book> books) {
        if (!user.isActive()) {
            throw new LoanBusinessException("비활성화된 사용자는 도서를 대출할 수 없습니다.");
        }

        long currentActiveLoans = bookLoanRepository.countActiveByUserId(user.getId());
        if (currentActiveLoans + books.size() > 5) {
            throw new LoanBusinessException(
                    String.format("최대 5권까지만 대출 가능합니다. 현재 대출 중: %d권", currentActiveLoans)
            );
        }

        for (Book book : books) {
            if (!book.canBorrow(1)) {
                throw new LoanBusinessException(
                        "대출할 수 없는 도서가 포함되어 있습니다: " + book.getTitle().getValue()
                );
            }
        }
    }

    /**
     * 대출 생성 및 실행
     */
    private BookLoan createAndExecuteLoan(Book book, User user, LoanPeriod loanPeriod) {
        BookLoan loan = BookLoan.create(book, user, LoanQuantity.of(1), loanPeriod);
        loan.executeLoan(); // 재고 감소
        return loan;
    }
}