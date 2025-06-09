package com.bookworm.application.service.Loan;

import com.bookworm.domain.entity.*;
import com.bookworm.domain.entity.BookLoan;
import com.bookworm.infrastructure.repository.BookLoanRepository;
import com.bookworm.domain.repository.BookRepository;
import com.bookworm.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 대출 관련 공통 헬퍼 클래스
 * - 엔티티 조회 및 검증 로직
 * - 예외 처리 통일
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoanHelper {

    private final BookLoanRepository bookLoanRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    /**
     * 대출 ID로 대출 조회 (없으면 예외)
     */
    public BookLoan getLoanOrThrow(Long loanId) {
        if (loanId == null) {
            throw new IllegalArgumentException("대출 ID는 필수입니다.");
        }

        return bookLoanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("대출을 찾을 수 없습니다: " + loanId));
    }

    /**
     * 사용자 ID로 사용자 조회 (없으면 예외)
     */
    public User getUserOrThrow(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }

    /**
     * 도서 ID로 도서 조회 (없으면 예외)
     */
    public Book getBookOrThrow(Long bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("도서 ID는 필수입니다.");
        }

        return bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("도서를 찾을 수 없습니다: " + bookId));
    }

    /**
     * 여러 도서 ID로 도서들 조회 (없으면 예외)
     */
    public List<Book> getBooksOrThrow(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            throw new IllegalArgumentException("도서 ID 목록은 필수입니다.");
        }

        List<Book> books = bookIds.stream()
                .map(this::getBookOrThrow)
                .collect(Collectors.toList());

        if (books.size() != bookIds.size()) {
            throw new IllegalArgumentException("일부 도서를 찾을 수 없습니다.");
        }

        return books;
    }

    /**
     * 이메일로 사용자 조회 (없으면 예외)
     */
    public User getUserByEmailOrThrow(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        return userRepository.findByEmail(com.bookworm.domain.vo.user.Email.of(email))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
    }

    /**
     * 대출 존재 여부 확인
     */
    public boolean loanExists(Long loanId) {
        return loanId != null && bookLoanRepository.existsById(loanId);
    }

    /**
     * 사용자 존재 여부 확인
     */
    public boolean userExists(Long userId) {
        return userId != null && userRepository.existsById(userId);
    }

    /**
     * 도서 존재 여부 확인
     */
    public boolean bookExists(Long bookId) {
        return bookId != null && bookRepository.existsById(bookId);
    }

    /**
     * 안전한 대출 조회 (존재하지 않으면 null 반환)
     */
    public BookLoan getLoanSafely(Long loanId) {
        if (loanId == null) {
            return null;
        }
        return bookLoanRepository.findById(loanId).orElse(null);
    }

    /**
     * 안전한 사용자 조회 (존재하지 않으면 null 반환)
     */
    public User getUserSafely(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * 안전한 도서 조회 (존재하지 않으면 null 반환)
     */
    public Book getBookSafely(Long bookId) {
        if (bookId == null) {
            return null;
        }
        return bookRepository.findById(bookId).orElse(null);
    }
}

