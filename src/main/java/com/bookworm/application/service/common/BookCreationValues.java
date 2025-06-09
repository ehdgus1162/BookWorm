package com.bookworm.application.service.common;

import com.bookworm.domain.vo.book.BookLanguage;
import com.bookworm.domain.vo.book.BookQuantity;
import com.bookworm.domain.vo.book.BookTitle;
import com.bookworm.domain.vo.book.BookType;

/**
 * 도서 생성 시 사용할 값 객체들
 */
public record BookCreationValues(
        BookTitle title,
        BookLanguage language,
        BookType type,
        BookQuantity quantity
) {}
