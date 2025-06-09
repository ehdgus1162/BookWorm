package com.bookworm.application.service.Loan;



import com.bookworm.domain.entity.Book;
import com.bookworm.domain.entity.BookLoan;
import com.bookworm.domain.entity.User;

import com.bookworm.domain.exception.LoanBusinessException;
import com.bookworm.infrastructure.repository.BookLoanRepository;
import com.bookworm.domain.vo.bookloan.LoanPeriod;
import com.bookworm.domain.vo.bookloan.LoanQuantity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 대출 도메인 서비스
 * - 복잡한 비즈니스 로직 처리
 * - 여러 엔티티 간의 협력 조율
 * - 도메인 규칙 검증
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanDomainService {

    private final BookLoanRepository bookLoanRepository;

    /**
     * 여러 도서 대출 생성
     */
    public List<BookLoan> createMultipleLoans(User user, List<Book> books, LoanPeriod loanPeriod) {
        // 대출 가능성 검증
        validateMultipleLoanCreation(user, books);

        // 각 도서별로 대출 생성 및 실행
        return books.stream()
                .map(book -> {
                    BookLoan loan = BookLoan.create(book, user, LoanQuantity.of(1), loanPeriod);
                    loan.executeLoan(); // 재고 감소
                    log.debug("대출 생성 및 실행 완료 - 도서: '{}', 사용자: '{}'",
                            book.getTitle().getValue(), user.getFullName());
                    return loan;
                })
                .collect(Collectors.toList());
    }

    /**
     * 단일 도서 대출 생성
     */
    public BookLoan createSingleLoan(User user, Book book, LoanQuantity quantity, LoanPeriod loanPeriod) {
        // 대출 가능성 검증
        validateSingleLoanCreation(user, book, quantity);

        // 대출 생성 및 실행
        BookLoan loan = BookLoan.create(book, user, quantity, loanPeriod);
        loan.executeLoan(); // 재고 감소

        log.debug("단일 대출 생성 및 실행 완료 - 도서: '{}', 사용자: '{}'",
                book.getTitle().getValue(), user.getFullName());

        return loan;
    }

    /**
     * 대출 연장 검증
     */
    public void validateLoanExtension(BookLoan loan, int days) {
        if (!loan.isActive()) {
            throw new LoanBusinessException("활성 상태가 아닌 대출은 연장할 수 없습니다.");
        }

        if (loan.isOverdue()) {
            throw new LoanBusinessException("연체된 대출은 연장할 수 없습니다.");
        }

        if (days <= 0 || days > 14) {
            throw new LoanBusinessException("연장 일수는 1일 이상 14일 이하여야 합니다.");
        }

        // 연장 후 총 대출 기간이 30일을 초과하지 않는지 확인
        long totalDays = loan.getLoanPeriod().getTotalLoanDays() + days;
        if (totalDays > 30) {
            throw new LoanBusinessException("총 대출 기간이 30일을 초과할 수 없습니다.");
        }

        log.debug("대출 연장 검증 통과 - 대출 ID: {}, 연장 일수: {}일", loan.getId(), days);
    }

    /**
     * 대출 취소 검증
     */
    public void validateLoanCancellation(BookLoan loan) {
        if (!loan.isActive()) {
            throw new LoanBusinessException("활성 상태가 아닌 대출은 취소할 수 없습니다.");
        }

        // 대출 당일에만 취소 가능하도록 제한 (선택적 규칙)
        if (!loan.getLoanPeriod().getLoanDate().equals(java.time.LocalDate.now())) {
            throw new LoanBusinessException("대출 당일에만 취소할 수 있습니다.");
        }

        log.debug("대출 취소 검증 통과 - 대출 ID: {}", loan.getId());
    }

    /**
     * 여러 도서 대출 생성 검증
     */
    private void validateMultipleLoanCreation(User user, List<Book> books) {
        // 사용자 상태 검증
        if (!user.isActive()) {
            throw new LoanBusinessException("비활성화된 사용자는 도서를 대출할 수 없습니다.");
        }

        // 현재 활성 대출 수 확인
        long currentActiveLoans = bookLoanRepository.countActiveByUserId(user.getId());
        if (currentActiveLoans + books.size() > 5) {
            throw new LoanBusinessException(
                    String.format("최대 5권까지만 대출 가능합니다. 현재 대출 중인 도서: %d권", currentActiveLoans)
            );
        }

        // 각 도서의 대출 가능성 확인
        for (Book book : books) {
            if (!book.canBorrow(1)) {
                throw new LoanBusinessException(
                        "대출할 수 없는 도서가 포함되어 있습니다: " + book.getTitle().getValue()
                );
            }
        }

        // 중복 도서 확인
        long uniqueBookCount = books.stream()
                .map(Book::getId)
                .distinct()
                .count();

        if (uniqueBookCount != books.size()) {
            throw new LoanBusinessException("동일한 도서를 중복 선택할 수 없습니다.");
        }

        log.debug("여러 도서 대출 생성 검증 통과 - 사용자: '{}', 도서 수: {}",
                user.getFullName(), books.size());
    }

    /**
     * 단일 도서 대출 생성 검증
     */
    private void validateSingleLoanCreation(User user, Book book, LoanQuantity quantity) {
        // 사용자 상태 검증
        if (!user.isActive()) {
            throw new LoanBusinessException("비활성화된 사용자는 도서를 대출할 수 없습니다.");
        }

        // 도서 대출 가능성 확인
        if (!book.canBorrow(quantity.getValue())) {
            throw new LoanBusinessException("해당 도서는 현재 대출할 수 없습니다: " + book.getTitle().getValue());
        }

        // 현재 활성 대출 수 확인
        long currentActiveLoans = bookLoanRepository.countActiveByUserId(user.getId());
        if (currentActiveLoans + quantity.getValue() > 5) {
            throw new LoanBusinessException(
                    String.format("최대 5권까지만 대출 가능합니다. 현재 대출 중인 도서: %d권", currentActiveLoans)
            );
        }

        log.debug("단일 도서 대출 생성 검증 통과 - 사용자: '{}', 도서: '{}'",
                user.getFullName(), book.getTitle().getValue());
    }
}