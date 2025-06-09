package com.bookworm.application.service.common;

import com.bookworm.application.dto.BookSearchRequest;
import com.bookworm.domain.constant.BookStatus;
import com.bookworm.domain.entity.Book;
import com.bookworm.domain.entity.QBook;
import com.bookworm.domain.vo.book.BookLanguage;
import com.bookworm.domain.vo.book.BookType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 도서 검색 쿼리 헬퍼
 * - QueryDSL 기반 동적 쿼리 처리
 * - 검색 조건별 분기 로직 제거
 */
@Component
@RequiredArgsConstructor
public class BookQueryHelper {

    private final JPAQueryFactory jpaQueryFactory;
    private static final QBook book = QBook.book;


    /**
     * 동적 조건으로 도서 검색
     */
    public Page<Book> searchBooks(BookSearchRequest request) {
        Pageable pageable = createPageable(request.page(), request.size());
        BooleanBuilder builder = createSearchConditions(request);

        List<Book> books = jpaQueryFactory
                .selectFrom(book)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(book.createdAt.desc())
                .fetch();

        long total = jpaQueryFactory
                .selectFrom(book)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(books, pageable, total);
    }


    /**
     * 검색 조건 생성
     */
    private BooleanBuilder createSearchConditions(BookSearchRequest request) {
        BooleanBuilder builder = new BooleanBuilder();

        // 제목 검색 (부분 일치, 대소문자 무시)
        Optional.ofNullable(request.titleKeyword())
                .filter(this::hasValue)
                .ifPresent(title -> builder.and(book.title.value.containsIgnoreCase(title.trim())));

        // 유형 검색
        Optional.ofNullable(request.type())
                .filter(this::hasValue)
                .ifPresent(type -> {
                    try {
                        BookType bookType = BookType.of(type);
                        builder.and(book.type.value.eq(bookType.getValue()));
                    } catch (IllegalArgumentException e) {
                        // 잘못된 유형은 무시 (빈 결과 반환)
                        builder.and(book.id.eq(-1L));
                    }
                });

        // 언어 검색
        Optional.ofNullable(request.language())
                .filter(this::hasValue)
                .ifPresent(language -> {
                    try {
                        BookLanguage bookLanguage = BookLanguage.of(language);
                        builder.and(book.language.value.eq(bookLanguage.getValue()));
                    } catch (IllegalArgumentException e) {
                        builder.and(book.id.eq(-1L));
                    }
                });

        // 상태 검색
        Optional.ofNullable(request.status())
                .filter(this::hasValue)
                .ifPresent(status -> {
                    try {
                        BookStatus bookStatus = BookStatus.valueOf(status.toUpperCase());
                        builder.and(book.status.eq(bookStatus));
                    } catch (IllegalArgumentException e) {
                        builder.and(book.id.eq(-1L));
                    }
                });

        return builder;
    }

    /**
     * Pageable 생성
     */
    public Pageable createPageable(Integer page, Integer size) {
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null && size > 0 && size <= 100 ? size : 20;

        return PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /**
     * 문자열 값 유무 확인
     */
    public boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

}