package com.bookworm.domain.vo.book;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 도서 언어 값 객체
 * - 지원되는 언어만 허용
 * - 대소문자 구분 없이 처리
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookLanguage {

    // 지원 언어 목록
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(
            "KOREAN", "ENGLISH", "JAPANESE", "CHINESE", "SPANISH", "FRENCH", "GERMAN"
    );

    private String value;

    private BookLanguage(String value) {
        this.value = value;
    }

    public static BookLanguage of(String value) {
        validateLanguage(value);
        return new BookLanguage(value.toUpperCase());
    }

    /**
     * 언어 유효성 검증
     */
    private static void validateLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            throw new IllegalArgumentException("도서 언어는 필수입니다.");
        }

        if (!SUPPORTED_LANGUAGES.contains(language.toUpperCase())) {
            throw new IllegalArgumentException("지원되지 않는 언어입니다: " + language +
                    ". 지원 언어: " + String.join(", ", SUPPORTED_LANGUAGES));
        }
    }

    /**
     * 지원 언어 목록 반환
     */
    public static List<String> getSupportedLanguages() {
        return List.copyOf(SUPPORTED_LANGUAGES);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookLanguage that = (BookLanguage) o;
        return Objects.equals(value, that.value);
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