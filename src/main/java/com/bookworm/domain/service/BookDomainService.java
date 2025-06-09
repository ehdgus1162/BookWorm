package com.bookworm.domain.service;


import com.bookworm.domain.entity.Book;
import com.bookworm.domain.entity.User;
import com.bookworm.domain.exception.BookBusinessException;
import com.bookworm.domain.repository.BookRepository;
import com.bookworm.domain.vo.book.BookLanguage;
import com.bookworm.domain.vo.book.BookQuantity;
import com.bookworm.domain.vo.book.BookTitle;
import com.bookworm.domain.vo.book.BookType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 도서 도메인 서비스
 * - 복잡한 비즈니스 로직 처리
 * - 도메인 규칙 검증
 * - 여러 엔티티 간의 협력이 필요한 로직
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookDomainService {

    private final BookRepository bookRepository;

    /**
     * 새로운 도서 등록
     * - 중복 도서 확인
     * - 중복 시 기존 도서 수량 증가 또는 새 도서 등록
     */
    public Book registerNewBook(BookTitle title, BookLanguage language, BookType type,
                                BookQuantity quantity, User registeredBy) {

        // 중복 도서 확인
        Optional<Book> existingBook = bookRepository.findSameBook(title, language, type);

        if (existingBook.isPresent()) {
            return handleDuplicateBook(existingBook.get(), quantity);
        }

        // 새로운 도서 생성
        Book newBook = Book.create(title, language, type, quantity, registeredBy);
        Book savedBook = bookRepository.save(newBook);

        log.info("새로운 도서 등록 완료 - ID: {}, 제목: {}, 수량: {}",
                savedBook.getId(), title.getValue(), quantity.getValue());

        return savedBook;
    }

    /**
     * 중복 도서 처리
     * - 기존 도서의 수량을 증가시킴
     */
    private Book handleDuplicateBook(Book existingBook, BookQuantity additionalQuantity) {
        log.info("중복 도서 발견 - 기존 도서 ID: {}, 추가 수량: {}",
                existingBook.getId(), additionalQuantity.getValue());

        existingBook.addStock(additionalQuantity.getValue());
        Book updatedBook = bookRepository.save(existingBook);

        log.info("기존 도서 재고 추가 완료 - ID: {}, 새로운 총 수량: {}",
                updatedBook.getId(), updatedBook.getQuantity().getValue());

        return updatedBook;
    }

    /**
     * 도서 정보 수정 검증
     * - 수정하려는 정보가 다른 도서와 중복되는지 확인
     */
    public void validateBookUpdate(Long bookId, BookTitle newTitle, BookLanguage newLanguage, BookType newType) {
        // 현재 도서 조회
        Book currentBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookBusinessException("수정하려는 도서를 찾을 수 없습니다."));

        // 변경하려는 정보가 현재와 같다면 검증 불필요
        if (currentBook.isSameBook(newTitle, newLanguage, newType)) {
            return;
        }

        // 변경하려는 정보와 동일한 다른 도서가 있는지 확인
        Optional<Book> duplicateBook = bookRepository.findSameBook(newTitle, newLanguage, newType);

        if (duplicateBook.isPresent() && !duplicateBook.get().getId().equals(bookId)) {
            throw new BookBusinessException(
                    String.format("동일한 도서가 이미 존재합니다. (제목: %s, 언어: %s, 유형: %s)",
                            newTitle.getValue(), newLanguage.getValue(), newType.getValue())
            );
        }
    }

    /**
     * 도서 삭제 가능 여부 검증
     * - 대출 중인 도서는 삭제 불가
     * - 예약된 도서는 삭제 불가
     */
    public void validateBookDeletion(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookBusinessException("삭제하려는 도서를 찾을 수 없습니다."));

        // 대출 중이거나 예약된 도서는 삭제 불가
        if (!book.getStatus().isOperational()) {
            throw new BookBusinessException("현재 상태에서는 도서를 삭제할 수 없습니다: " +
                    book.getStatus().getDescription());
        }

        // 추가로 실제 대출 기록이 있는지 확인 (향후 BookLoan 구현 시)
        // if (hasActiveLoan(bookId)) {
        //     throw new BookBusinessException("대출 기록이 있는 도서는 삭제할 수 없습니다.");
        // }
    }

    /**
     * 도서 대출 가능 여부 검증
     * - 향후 대출 시스템 구현 시 사용
     */
    public boolean canBorrowBook(Long bookId, Integer requestedQuantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookBusinessException("도서를 찾을 수 없습니다."));

        return book.canBorrow(requestedQuantity);
    }

    /**
     * 재고 부족 도서 목록 조회를 위한 비즈니스 로직
     */
    public boolean isLowStock(Book book, Integer threshold) {
        return book.getQuantity().getValue() <= threshold;
    }
}
