package com.bookworm.domain.repository;


import com.bookworm.domain.constant.BookStatus;
import com.bookworm.domain.entity.Book;
import com.bookworm.domain.vo.book.BookLanguage;
import com.bookworm.domain.vo.book.BookTitle;
import com.bookworm.domain.vo.book.BookType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 도메인 BookRepository 인터페이스
 * - 도메인 계층의 리포지토리 계약
 * - 인프라스트럭처에 의존하지 않는 순수한 도메인 인터페이스
 * - 비즈니스 중심의 메서드 정의
 */
public interface BookRepository {

    /**
     * 도서 저장
     */
    Book save(Book book);

    /**
     * ID로 도서 조회
     */
    Optional<Book> findById(Long id);

    /**
     * 모든 도서 조회 (페이징)
     */
    Page<Book> findAll(Pageable pageable);

    /**
     * 도서 삭제
     */
    void delete(Book book);

    /**
     * ID로 도서 삭제
     */
    void deleteById(Long id);

    /**
     * 도서 존재 여부 확인
     */
    boolean existsById(Long id);

    /**
     * 전체 도서 수 조회
     */
    long count();

    // === 비즈니스 중심 조회 메서드 ===

    /**
     * 동일한 도서 찾기 (제목, 언어, 유형이 모두 같은 도서)
     */
    Optional<Book> findSameBook(BookTitle title, BookLanguage language, BookType type);

    /**
     * 제목으로 도서 검색 (부분 일치)
     */
    List<Book> findByTitleContaining(String titleKeyword);

    /**
     * 도서 유형으로 조회
     */
    List<Book> findByType(BookType type);

    /**
     * 도서 언어로 조회
     */
    List<Book> findByLanguage(BookLanguage language);

    /**
     * 도서 상태로 조회
     */
    List<Book> findByStatus(BookStatus status);

    /**
     * 이용 가능한 도서 조회 (재고가 있는 도서)
     */
    List<Book> findAvailableBooks();

    /**
     * 대출 중인 도서 조회 (재고가 0인 도서)
     */
    List<Book> findBorrowedBooks();

    /**
     * 특정 관리자가 등록한 도서 조회
     */
    List<Book> findByRegisteredBy(Long userId);

    /**
     * 제목과 유형으로 복합 검색
     */
    Page<Book> findByTitleContainingAndType(String titleKeyword, BookType type, Pageable pageable);

    /**
     * 제목으로 검색 (페이징)
     */
    Page<Book> findByTitleContaining(String titleKeyword, Pageable pageable);

    /**
     * ID 목록으로 도서 조회
     */
    List<Book> findByIdIn(List<Long> ids);
}