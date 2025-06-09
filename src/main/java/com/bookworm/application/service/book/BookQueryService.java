package com.bookworm.application.service.book;

import com.bookworm.application.dto.BookOptionsResponse;
import com.bookworm.application.dto.BookPageResponse;
import com.bookworm.application.dto.BookResponse;
import com.bookworm.application.dto.BookSearchRequest;
import com.bookworm.application.service.common.*;
import com.bookworm.domain.entity.Book;
import com.bookworm.domain.repository.BookRepository;
import com.bookworm.domain.repository.BookStatisticsRepository;
import com.bookworm.domain.vo.book.BookLanguage;
import com.bookworm.domain.vo.book.BookType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 도서 Query Service (읽기 작업 전용)
 * - 조회, 검색, 통계
 * - 읽기 전용 트랜잭션
 * - 성능 최적화 집중
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@Validated
public class BookQueryService {

    private final BookRepository bookRepository;
    private final BookStatisticsRepository bookStatisticsRepository;
    private final BookHelper bookHelper;
    private final BookQueryHelper bookQueryHelper;
    private final BookMapper bookMapper;

    /**
     * 도서 상세 조회
     */
    public BookResponse getBook(Long bookId) {
        Book book = bookHelper.getBookOrThrow(bookId);
        return bookMapper.toBookResponse(book);
    }

    /**
     * 모든 도서 조회 (페이징)
     */
    public BookPageResponse getAllBooks(Integer page, Integer size) {
        org.springframework.data.domain.Pageable pageable = bookQueryHelper.createPageable(page, size);
        Page<Book> bookPage = bookRepository.findAll(pageable);
        return bookMapper.toBookPageResponse(bookPage);
    }

    /**
     * 도서 검색 (통합 검색)
     */
    public BookPageResponse searchBooks(@Valid BookSearchRequest request) {
        Page<Book> bookPage = bookQueryHelper.searchBooks(request);
        return bookMapper.toBookPageResponse(bookPage);
    }

    /**
     * 이용 가능한 도서 조회
     */
    public List<BookResponse> getAvailableBooks() {
        List<Book> books = bookRepository.findAvailableBooks();
        return bookMapper.toBookResponseList(books);
    }

    /**
     * 대출 중인 도서 조회
     */
    public List<BookResponse> getBorrowedBooks() {
        List<Book> books = bookRepository.findBorrowedBooks();
        return bookMapper.toBookResponseList(books);
    }

    /**
     * 특정 관리자가 등록한 도서 조회
     */
    public List<BookResponse> getBooksRegisteredBy(String userEmail) {
        com.bookworm.domain.entity.User user = bookHelper.getUserOrThrow(userEmail);
        List<Book> books = bookRepository.findByRegisteredBy(user.getId());
        return bookMapper.toBookResponseList(books);
    }

    /**
     * 도서 통계 조회
     */
    public BookStatisticsResponse getBookStatistics() {
        BookStatisticsData statisticsData = new BookStatisticsData(
                bookStatisticsRepository.getBookCountByStatus(),
                bookStatisticsRepository.getBookCountByType(),
                bookStatisticsRepository.getBookCountByLanguage(),
                bookStatisticsRepository.getTotalQuantity(),
                bookStatisticsRepository.getAvailableQuantity(),
                bookStatisticsRepository.getBorrowedQuantity()
        );

        return BookStatisticsResponse.from(statisticsData);
    }

    /**
     * 지원되는 옵션 목록 조회
     */
    public BookOptionsResponse getBookOptions() {
        List<String> languages = BookLanguage.getSupportedLanguages();
        List<String> types = BookType.getBookTypes();
        return BookOptionsResponse.create(languages, types);
    }

    /**
     * 전체 도서 수 조회
     */
    public long getTotalBookCount() {
        return bookRepository.count();
    }
}