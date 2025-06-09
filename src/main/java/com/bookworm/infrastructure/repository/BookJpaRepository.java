package com.bookworm.infrastructure.repository;

import com.bookworm.domain.constant.BookStatus;
import com.bookworm.domain.entity.Book;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface BookJpaRepository extends JpaRepository<Book, Long> {

    /**
     * 동일한 도서 찾기 (제목, 언어, 유형으로)
     * - 중복 도서 등록 방지를 위한 메서드
     */

    @Query("SELECT b FROM Book b WHERE b.title.value = :title AND b.language.value = :language AND b.type.value = :type")
    Optional<Book> findByTitleAndLanguageAndType(@Param("title") String title,
                                                 @Param("language") String language,
                                                 @Param("type") String type);


    /**
     * 제목으로 도서 검색 (부분 일치, 대소문자 무시)
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title.value) LIKE LOWER(CONCAT('%', :titleKeyword, '%'))")
    List<Book> findByTitleContainingIgnoreCase(@Param("titleKeyword") String titleKeyword);


    /**
     * 제목으로 도서 검색 (페이징, 부분 일치, 대소문자 무시)
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title.value) LIKE LOWER(CONCAT('%', :titleKeyword, '%'))")
    Page<Book> findByTitleContainingIgnoreCase(@Param("titleKeyword") String titleKeyword, Pageable pageable);

    /**
     * 도서 유형으로 조회
     */
    @Query("SELECT b FROM Book b WHERE b.type.value = :type")
    List<Book> findByType(@Param("type") String type);


    /**
     * 도서 언어로 조회
     */
    @Query("SELECT b FROM Book b WHERE b.language.value = :language")
    List<Book> findByLanguage(@Param("language") String language);

    /**
     * 도서 상태로 조회
     */
    List<Book> findByStatus(BookStatus status);

    /**
     * 이용 가능한 도서 조회 (상태가 AVAILABLE이고 재고가 있는 도서)
     */
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND b.quantity.value > 0")
    List<Book> findAvailableBooks();

    /**
     * 대출 중인 도서 조회 (재고가 0이거나 상태가 BORROWED인 도서)
     */
    @Query("SELECT b FROM Book b WHERE b.quantity.value = 0 OR b.status = 'BORROWED'")
    List<Book> findBorrowedBooks();

    /**
     * 특정 관리자가 등록한 도서 조회
     */
    @Query("SELECT b FROM Book b WHERE b.registeredBy.id = :userId")
    List<Book> findByRegisteredBy(@Param("userId") Long userId);

    /**
     * 제목과 유형으로 복합 검색 (페이징)
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title.value) LIKE LOWER(CONCAT('%', :titleKeyword, '%')) AND b.type.value = :type")
    Page<Book> findByTitleContainingAndType(@Param("titleKeyword") String titleKeyword,
                                            @Param("type") String type,
                                            Pageable pageable);

    /**
     * ID 목록으로 도서 조회
     */
    List<Book> findByIdIn(List<Long> ids);

    /**
     * 재고 수량별 도서 조회
     */
    @Query("SELECT b FROM Book b WHERE b.quantity.value >= :minQuantity")
    List<Book> findByQuantityGreaterThanEqual(@Param("minQuantity") Integer minQuantity);

    /**
     * 도서 통계 - 상태별 개수
     */
    @Query("SELECT b.status, COUNT(b) FROM Book b GROUP BY b.status")
    List<Object[]> countByStatus();

    /**
     * 도서 통계 - 유형별 개수
     */
    @Query("SELECT b.type.value, COUNT(b) FROM Book b GROUP BY b.type.value")
    List<Object[]> countByType();

    /**
     * 도서 통계 - 언어별 개수
     */
    @Query("SELECT b.language.value, COUNT(b) FROM Book b GROUP BY b.language.value")
    List<Object[]> countByLanguage();

    /**
     * 전체 재고 수량 합계
     */
    @Query("SELECT SUM(b.quantity.value) FROM Book b")
    Long sumTotalQuantity();

    /**
     * 이용 가능한 총 재고 수량
     */
    @Query("SELECT SUM(b.quantity.value) FROM Book b WHERE b.status = 'AVAILABLE'")
    Long sumAvailableQuantity();
}
