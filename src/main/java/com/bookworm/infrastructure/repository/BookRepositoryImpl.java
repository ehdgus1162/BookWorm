package com.bookworm.infrastructure.repository;

import com.bookworm.domain.constant.BookStatus;
import com.bookworm.domain.entity.Book;
import com.bookworm.domain.repository.BookRepository;
import com.bookworm.domain.vo.book.BookLanguage;
import com.bookworm.domain.vo.book.BookTitle;
import com.bookworm.domain.vo.book.BookType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * BookRepository 구현체
 * - 도메인 인터페이스와 JPA Repository 연결
 * - 값 객체와 원시 타입 간의 변환 처리
 * - 예외 처리 및 로깅
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class BookRepositoryImpl implements BookRepository {


    private final BookJpaRepository bookJpaRepository;

    @Override
    public Book save(Book book) {
        try {
            Book savedBook = bookJpaRepository.save(book);
            log.debug("도서 저장 완료: {}", savedBook);
            return savedBook;
        } catch (Exception e) {
            log.error("도서 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("도서 저장에 실패했습니다", e);
        }
    }

    @Override
    public Optional<Book> findById(Long id) {
        try {
            return bookJpaRepository.findById(id);
        } catch (Exception e) {
            log.error("도서 ID {} 조회 실패: {}", id, e.getMessage(), e);
            throw new RuntimeException("도서 조회에 실패했습니다.", e);
        }
    }

    @Override
    public Page<Book> findAll(Pageable pageable) {
        try {
            return bookJpaRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("도서 목록 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("도서 목록 조회에 실패했습니다.", e);
        }
    }

    @Override
    public void delete(Book book) {
        try {
            bookJpaRepository.delete(book);
            log.debug("도서 삭제 완료: {}", book);
        } catch (Exception e) {
            log.error("도서 삭제 실패: {}", e.getMessage(), e);
            throw new RuntimeException("도서 삭제에 실패했습니다.", e);
        }
    }


    @Override
    public void deleteById(Long id) {
        try {
            bookJpaRepository.deleteById(id);
            log.debug("도서 ID {} 삭제 완료", id);
        } catch (Exception e) {
            log.error("도서 ID {} 삭제 실패: {}", id, e.getMessage(), e);
            throw new RuntimeException("도서 삭제에 실패했습니다.", e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        try {
            return bookJpaRepository.existsById(id);
        } catch (Exception e) {
            log.error("도서 ID {} 존재 여부 확인 실패: {}", id, e.getMessage(), e);
            throw new RuntimeException("도서 존재 여부 확인에 실패했습니다.", e);
        }
    }

    @Override
    public long count() {
        try {
            return bookJpaRepository.count();
        } catch (Exception e) {
            log.error("전체 도서 수 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("전체 도서 수 조회에 실패했습니다.", e);
        }
    }

    @Override
    public Optional<Book> findSameBook(BookTitle title, BookLanguage language, BookType type) {
        try {
            return bookJpaRepository.findByTitleAndLanguageAndType(
                    title.getValue(),
                    language.getValue(),
                    type.getValue()
            );
        } catch (Exception e) {
            log.error("동일 도서 검색 실패 - 제목: {}, 언어: {}, 유형: {}",
                    title.getValue(), language.getValue(), type.getValue(), e);
            throw new RuntimeException("동일 도서 검색에 실패했습니다.", e);
        }
    }

    @Override
    public List<Book> findByTitleContaining(String titleKeyword) {
        try {
            return bookJpaRepository.findByTitleContainingIgnoreCase(titleKeyword);
        } catch (Exception e) {
            log.error("제목 검색 실패 - 키워드: {}", titleKeyword, e);
            throw new RuntimeException("제목 검색에 실패했습니다.", e);
        }
    }

    @Override
    public List<Book> findByType(BookType type) {
        try {
            return bookJpaRepository.findByType(type.getValue());
        } catch (Exception e) {
            log.error("유형별 도서 조회 실패 - 유형: {}", type.getValue(), e);
            throw new RuntimeException("유형별 도서 조회에 실패했습니다.", e);
        }
    }

    @Override
    public List<Book> findByLanguage(BookLanguage language) {
        try {
            return bookJpaRepository.findByLanguage(language.getValue());
        } catch (Exception e) {
            log.error("언어별 도서 조회 실패 - 언어: {}", language.getValue(), e);
            throw new RuntimeException("언어별 도서 조회에 실패했습니다.", e);
        }
    }

    @Override
    public List<Book> findByStatus(BookStatus status) {
        try {
            return bookJpaRepository.findByStatus(status);
        } catch (Exception e) {
            log.error("상태별 도서 조회 실패 - 상태: {}", status, e);
            throw new RuntimeException("상태별 도서 조회에 실패했습니다.", e);
        }
    }

    @Override
    public List<Book> findAvailableBooks() {
        try {
            return bookJpaRepository.findAvailableBooks();
        } catch (Exception e) {
            log.error("이용 가능한 도서 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이용 가능한 도서 조회에 실패했습니다.", e);
        }
    }

    @Override
    public List<Book> findBorrowedBooks() {
        try {
            return bookJpaRepository.findBorrowedBooks();
        } catch (Exception e) {
            log.error("대출 중인 도서 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("대출 중인 도서 조회에 실패했습니다.", e);
        }
    }

    @Override
    public List<Book> findByRegisteredBy(Long userId) {
        try {
            return bookJpaRepository.findByRegisteredBy(userId);
        } catch (Exception e) {
            log.error("등록자별 도서 조회 실패 - 사용자 ID: {}", userId, e);
            throw new RuntimeException("등록자별 도서 조회에 실패했습니다.", e);
        }
    }

    @Override
    public Page<Book> findByTitleContainingAndType(String titleKeyword, BookType type, Pageable pageable) {
        try {
            return bookJpaRepository.findByTitleContainingAndType(
                    titleKeyword,
                    type.getValue(),
                    pageable
            );
        } catch (Exception e) {
            log.error("제목+유형 복합 검색 실패 - 키워드: {}, 유형: {}", titleKeyword, type.getValue(), e);
            throw new RuntimeException("복합 검색에 실패했습니다.", e);
        }
    }

    @Override
    public Page<Book> findByTitleContaining(String titleKeyword, Pageable pageable) {
        try {
            return bookJpaRepository.findByTitleContainingIgnoreCase(titleKeyword, pageable);
        } catch (Exception e) {
            log.error("제목 검색 실패 (페이징) - 키워드: {}", titleKeyword, e);
            throw new RuntimeException("제목 검색에 실패했습니다.", e);
        }
    }

    @Override
    public List<Book> findByIdIn(List<Long> ids) {
        try {
            return bookJpaRepository.findByIdIn(ids);
        } catch (Exception e) {
            log.error("ID 목록으로 도서 조회 실패 - IDs: {}", ids, e);
            throw new RuntimeException("ID 목록으로 도서 조회에 실패했습니다.", e);
        }
    }
}