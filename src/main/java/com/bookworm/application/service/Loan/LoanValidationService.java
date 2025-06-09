package com.bookworm.application.service.Loan;

import com.bookworm.domain.entity.Book;
import com.bookworm.domain.entity.User;
import com.bookworm.domain.vo.bookloan.LoanQuantity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 대출 입력 검증 서비스
 * - 클라이언트 입력값 검증
 * - 비즈니스 규칙 검증
 * - 검증 오류 수집 및 반환
 */
@Service
@Slf4j
public class LoanValidationService {

    /**
     * 대출 생성 시 입력값 검증
     */
    public ValidationResult validateLoanInput(Long userId, List<Long> bookIds,
                                              Integer quantity, LocalDate dueDate) {
        List<String> errors = new ArrayList<>();

        // 사용자 ID 검증
        if (userId == null) {
            errors.add("사용자 ID는 필수입니다.");
        }

        // 도서 ID 목록 검증
        if (bookIds == null || bookIds.isEmpty()) {
            errors.add("대출할 도서를 선택해주세요.");
        } else if (bookIds.size() > 5) {
            errors.add("한 번에 대출할 수 있는 최대 도서 수는 5권입니다.");
        }

        // 수량 검증
        try {
            if (quantity != null) {
                LoanQuantity.of(quantity);
            }
        } catch (IllegalArgumentException e) {
            errors.add("수량: " + e.getMessage());
        }

        // 반납 예정일 검증
        if (dueDate != null) {
            if (dueDate.isBefore(LocalDate.now())) {
                errors.add("반납 예정일은 오늘 이후여야 합니다.");
            }
            if (dueDate.isAfter(LocalDate.now().plusDays(30))) {
                errors.add("대출 기간은 최대 30일까지 가능합니다.");
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * 대출 가능성 검증 (비즈니스 규칙)
     */
    public ValidationResult validateLoanEligibility(User user, List<Book> books,
                                                    long currentActiveLoans) {
        List<String> errors = new ArrayList<>();

        // 사용자 상태 검증
        if (!user.isActive()) {
            errors.add("비활성화된 사용자는 도서를 대출할 수 없습니다.");
        }

        // 최대 대출 수량 검증
        if (currentActiveLoans + books.size() > 5) {
            errors.add("최대 5권까지만 대출 가능합니다. 현재 대출 중인 도서: " + currentActiveLoans + "권");
        }

        // 각 도서의 대출 가능성 검증
        for (Book book : books) {
            if (!book.canBorrow(1)) {
                errors.add("대출할 수 없는 도서가 포함되어 있습니다: " + book.getTitle().getValue());
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * 대출 연장 검증
     */
    public ValidationResult validateLoanExtension(int extensionDays, LocalDate currentDueDate,
                                                  boolean isOverdue) {
        List<String> errors = new ArrayList<>();

        if (extensionDays <= 0) {
            errors.add("연장 일수는 1일 이상이어야 합니다.");
        }

        if (extensionDays > 14) {
            errors.add("최대 14일까지만 연장 가능합니다.");
        }

        if (isOverdue) {
            errors.add("연체된 대출은 연장할 수 없습니다.");
        }

        LocalDate newDueDate = currentDueDate.plusDays(extensionDays);
        if (newDueDate.isAfter(LocalDate.now().plusDays(30))) {
            errors.add("총 대출 기간이 30일을 초과할 수 없습니다.");
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

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return String.join(", ", errors);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
}