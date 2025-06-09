package com.bookworm.domain.vo.book;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 도서 유형 값 객체
 * - 도서관에서 관리하는 도서 분류
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookType {

    // 도서 유형 목록
    private static final List<String> BOOK_TYPES = Arrays.asList(
            "FICTION", "NON_FICTION", "SCIENCE", "TECHNOLOGY", "HISTORY",
            "BIOGRAPHY", "REFERENCE", "TEXTBOOK", "CHILDREN", "COMIC"
    );

    private String value;

    private BookType(String value) {
        this.value = value;
    }

    public static BookType of(String value) {
        validateType(value);
        return new BookType(value.toUpperCase());
    }

    /**
     * 도서 유형 유효성 검증
     */
    private static void validateType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("도서 유형은 필수입니다.");
        }

        if (!BOOK_TYPES.contains(type.toUpperCase())) {
            throw new IllegalArgumentException("지원되지 않는 도서 유형입니다: " + type +
                    ". 지원 유형: " + String.join(", ", BOOK_TYPES));
        }
    }

    /**
     * 지원 도서 유형 목록 반환
     */
    public static List<String> getBookTypes() {
        return List.copyOf(BOOK_TYPES);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookType bookType = (BookType) o;
        return Objects.equals(value, bookType.value);
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