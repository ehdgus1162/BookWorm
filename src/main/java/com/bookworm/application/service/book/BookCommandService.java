package com.bookworm.application.service.book;

import com.bookworm.application.dto.*;
import com.bookworm.application.service.common.BookCreationValues;
import com.bookworm.application.service.common.BookHelper;
import com.bookworm.application.service.common.BookMapper;
import com.bookworm.application.service.common.BookUpdateValues;
import com.bookworm.domain.constant.BookStatus;
import com.bookworm.domain.entity.Book;
import com.bookworm.domain.entity.User;
import com.bookworm.domain.exception.BookBusinessException;
import com.bookworm.domain.repository.BookRepository;
import com.bookworm.domain.service.BookDomainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * 도서 Command Service (쓰기 작업 전용)
 * - 생성, 수정, 삭제, 상태변경, 재고추가
 * - JSR-303 검증 적용
 * - 비즈니스 로직에만 집중
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@Validated
public class BookCommandService {

    private final BookRepository bookRepository;
    private final BookDomainService bookDomainService;
    private final BookHelper bookHelper;
    private final BookMapper bookMapper;

    /**
     * 새로운 도서 등록
     */
    public BookResponse createBook(@Valid CreateBookRequest request, String registeredByEmail) {
        // DTO → 값 객체 변환
        BookCreationValues values = bookMapper.toBookCreationValues(request);
        User registeredBy = bookHelper.getUserOrThrow(registeredByEmail);

        // 도메인 서비스를 통한 도서 등록
        Book createdBook = bookDomainService.registerNewBook(
                values.title(), values.language(), values.type(), values.quantity(), registeredBy
        );

        log.info("도서 등록 완료 - 등록자: {}, 도서 ID: {}", registeredByEmail, createdBook.getId());
        return bookMapper.toBookResponse(createdBook);
    }

    /**
     * 도서 정보 수정
     */
    public BookResponse updateBook(Long bookId, @Valid UpdateBookRequest request) {
        // DTO → 값 객체 변환
        BookUpdateValues values = bookMapper.toBookUpdateValues(request);

        // 수정 전 검증
        bookDomainService.validateBookUpdate(bookId, values.title(), values.language(), values.type());

        // 도서 조회 및 수정
        Book book = bookHelper.getBookOrThrow(bookId);
        book.updateInfo(values.title(), values.language(), values.type(), values.quantity());
        Book updatedBook = bookRepository.save(book);

        log.info("도서 정보 수정 완료 - ID: {}", bookId);
        return bookMapper.toBookResponse(updatedBook);
    }

    /**
     * 도서 삭제
     */
    public void deleteBook(Long bookId) {
        // 삭제 가능 여부 검증
        bookDomainService.validateBookDeletion(bookId);

        // 도서 삭제
        bookRepository.deleteById(bookId);

        log.info("도서 삭제 완료 - ID: {}", bookId);
    }

    /**
     * 도서 상태 변경
     */
    public BookResponse changeBookStatus(Long bookId, @Valid BookStatusChangeRequest request) {
        BookStatus newStatus = parseBookStatus(request.status());
        Book book = bookHelper.getBookOrThrow(bookId);

        book.changeStatus(newStatus);
        Book updatedBook = bookRepository.save(book);

        log.info("도서 상태 변경 완료 - ID: {}, 새로운 상태: {}", bookId, newStatus);
        return bookMapper.toBookResponse(updatedBook);
    }

    /**
     * 재고 추가
     */
    public BookResponse addBookStock(Long bookId, @Valid AddStockRequest request) {
        Book book = bookHelper.getBookOrThrow(bookId);

        book.addStock(request.additionalQuantity());
        Book updatedBook = bookRepository.save(book);

        log.info("도서 재고 추가 완료 - ID: {}, 추가 수량: {}, 총 수량: {}",
                bookId, request.additionalQuantity(), updatedBook.getQuantity().getValue());

        return bookMapper.toBookResponse(updatedBook);
    }

    /**
     * 도서 상태 파싱
     */
    private BookStatus parseBookStatus(String status) {
        try {
            return BookStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BookBusinessException("유효하지 않은 도서 상태입니다: " + status);
        }
    }
}