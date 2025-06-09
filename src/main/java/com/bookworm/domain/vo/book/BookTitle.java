package com.bookworm.domain.vo.book;


import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 도서 제목 값 객체
 * - 원시값 포장을 통한 타입 안전성 보장
 * - 불변 객체로 설계
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookTitle {

    private String value;

    private BookTitle(String value) {
        this.value = value;
    }

    /**
     * 정적 팩토리 메서드
     */
    public static BookTitle of(String value) {
        validateTitle(value);
        return new BookTitle(value.trim());
    }

    /**
     * 제목 유효성 검증
     * - 조기 반환 패턴 사용
     */
    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("도서 제목은 필수입니다.");
        }

        if (title.trim().length() > 200) {
            throw new IllegalArgumentException("도서 제목은 200자를 초과할 수 없습니다.");
        }

        if (title.trim().length() < 1) {
            throw new IllegalArgumentException("도서 제목은 최소 1자 이상이어야 합니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookTitle bookTitle = (BookTitle) o;
        return Objects.equals(value, bookTitle.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}