package com.bookworm.application.service.book;

import com.bookworm.domain.vo.book.BookLanguage;
import com.bookworm.domain.vo.book.BookQuantity;
import com.bookworm.domain.vo.book.BookTitle;
import com.bookworm.domain.vo.book.BookType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 도서 입력 검증 서비스
 * - 클라이언트 입력값 검증
 * - 비즈니스 규칙 검증
 * - 검증 오류 수집 및 반환
 */
@Service
@Slf4j
public class BookValidationService {

    /**
     * 도서 등록/수정 시 입력값 검증
     */
    public ValidationResult validateBookInput(String title, String language, String type, Integer quantity) {
        List<String> errors = new ArrayList<>();

        // 제목 검증
        try {
            if (title != null) {
                BookTitle.of(title);
            }
        } catch (IllegalArgumentException e) {
            errors.add("제목: " + e.getMessage());
        }

        // 언어 검증
        try {
            if (language != null) {
                BookLanguage.of(language);
            }
        } catch (IllegalArgumentException e) {
            errors.add("언어: " + e.getMessage());
        }

        // 유형 검증
        try {
            if (type != null) {
                BookType.of(type);
            }
        } catch (IllegalArgumentException e) {
            errors.add("유형: " + e.getMessage());
        }

        // 수량 검증
        try {
            if (quantity != null) {
                BookQuantity.of(quantity);
            }
        } catch (IllegalArgumentException e) {
            errors.add("수량: " + e.getMessage());
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * 검증 결과 클래스
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public String getErrorMessage() { return String.join(", ", errors); }
    }
}