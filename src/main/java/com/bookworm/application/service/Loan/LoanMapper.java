package com.bookworm.application.service.Loan;

import com.bookworm.application.dto.*;
import com.bookworm.domain.entity.Book;
import com.bookworm.domain.entity.BookLoan;
import com.bookworm.domain.entity.User;

import com.bookworm.domain.vo.bookloan.LoanPeriod;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 간단한 대출 매퍼 클래스
 * - LoanResponse는 자체 정적 팩토리 메서드 사용
 * - 복잡한 변환만 처리
 */
@Component
public class LoanMapper {

    /**
     * BookLoan → LoanResponse 변환 (LoanResponse.from() 위임)
     */
    public LoanResponse toLoanResponse(BookLoan loan) {
        return LoanResponse.from(loan);
    }

    /**
     * BookLoan 리스트 → LoanResponse 리스트 변환
     */
    public List<LoanResponse> toLoanResponseList(List<BookLoan> loans) {
        if (loans == null || loans.isEmpty()) {
            return List.of();
        }
        return loans.stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Page<BookLoan> → LoanPageResponse 변환
     */
    public LoanPageResponse toLoanPageResponse(Page<BookLoan> loanPage) {
        if (loanPage == null) {
            return new LoanPageResponse(List.of(), 0, 0, 0, 0, true, true);
        }

        List<LoanResponse> loanResponses = loanPage.getContent().stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());

        return new LoanPageResponse(
                loanResponses,
                loanPage.getNumber(),
                loanPage.getSize(),
                loanPage.getTotalElements(),
                loanPage.getTotalPages(),
                loanPage.isFirst(),
                loanPage.isLast()
        );
    }

    /**
     * 여러 도서 대출 → BorrowResponseDto 변환
     */
    public BorrowResponseDto toBorrowResponseDto(User user, List<Book> books,
                                                 BookLoan representativeLoan, LoanPeriod loanPeriod) {
        List<BookDto> borrowedBookDtos = books.stream()
                .map(book -> new BookDto(
                        book.getId(),
                        book.getTitle().getValue(),
                        book.getType().getValue(),
                        book.getLanguage().getValue(),
                        "Borrowed",
                        book.getQuantity().getValue()
                ))
                .collect(Collectors.toList());

        return new BorrowResponseDto(
                representativeLoan.getId(),
                user.getId(),
                user.getFullName(),
                borrowedBookDtos,
                loanPeriod.getLoanDate(),
                loanPeriod.getDueDate(),
                books.size()
        );
    }
}