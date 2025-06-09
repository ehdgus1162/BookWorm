package com.bookworm.application.dto;


import com.bookworm.domain.entity.BookLoan;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * 대출 응답 DTO
 * - 클라이언트에게 대출 정보 전달
 * - 민감한 정보는 제외하고 필요한 정보만 포함
 * - JSON 직렬화 최적화
 */
public record LoanResponse(
        Long id,

        // 도서 정보
        Long bookId,
        String bookTitle,
        String bookType,
        String bookLanguage,

        // 사용자 정보
        Long userId,
        String userName,
        String userEmail,

        // 대출 정보
        Integer quantity,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate loanDate,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dueDate,

        String status,
        String statusDescription,

        // 상태 정보
        boolean isOverdue,
        boolean isActive,
        boolean isReturned,
        long daysUntilDue,
        long overdueDays,

        // 메타 정보
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        java.time.LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        java.time.LocalDateTime updatedAt
) {

    /**
     * BookLoan 엔티티로부터 LoanResponse 객체 생성
     */
    public static LoanResponse from(BookLoan bookLoan) {
        if (bookLoan == null) {
            return null;
        }

        return new LoanResponse(
                bookLoan.getId(),

                // 도서 정보
                bookLoan.getBook().getId(),
                bookLoan.getBook().getTitle().getValue(),
                bookLoan.getBook().getType().getValue(),
                bookLoan.getBook().getLanguage().getValue(),

                // 사용자 정보
                bookLoan.getUser().getId(),
                bookLoan.getUser().getFullName(),
                bookLoan.getUser().getEmail().getValue(),

                // 대출 정보
                bookLoan.getQuantity().getValue(),
                bookLoan.getLoanPeriod().getLoanDate(),
                bookLoan.getLoanPeriod().getDueDate(),
                bookLoan.getStatus().name(),
                bookLoan.getStatus().getDescription(),

                // 상태 정보
                bookLoan.isOverdue(),
                bookLoan.isActive(),
                bookLoan.isReturned(),
                bookLoan.getDaysUntilDue(),
                bookLoan.getLoanPeriod().getOverdueDays(),

                // 메타 정보
                bookLoan.getCreatedAt(),
                bookLoan.getUpdatedAt()
        );
    }

    /**
     * 간단한 LoanResponse 생성 (필수 정보만)
     */
    public static LoanResponse simple(BookLoan bookLoan) {
        if (bookLoan == null) {
            return null;
        }

        return new LoanResponse(
                bookLoan.getId(),
                bookLoan.getBook().getId(),
                bookLoan.getBook().getTitle().getValue(),
                bookLoan.getBook().getType().getValue(),
                bookLoan.getBook().getLanguage().getValue(),
                bookLoan.getUser().getId(),
                bookLoan.getUser().getFullName(),
                null, // 이메일 숨김
                bookLoan.getQuantity().getValue(),
                bookLoan.getLoanPeriod().getLoanDate(),
                bookLoan.getLoanPeriod().getDueDate(),
                bookLoan.getStatus().name(),
                bookLoan.getStatus().getDescription(),
                bookLoan.isOverdue(),
                bookLoan.isActive(),
                bookLoan.isReturned(),
                bookLoan.getDaysUntilDue(),
                bookLoan.getLoanPeriod().getOverdueDays(),
                bookLoan.getCreatedAt(),
                bookLoan.getUpdatedAt()
        );
    }

    /**
     * 관리자용 LoanResponse 생성 (모든 정보 포함)
     */
    public static LoanResponse forAdmin(BookLoan bookLoan) {
        // 기본 from() 메서드와 동일하지만, 향후 관리자 전용 필드 추가 가능
        return from(bookLoan);
    }

    /**
     * 사용자용 LoanResponse 생성 (개인정보 최소화)
     */
    public static LoanResponse forUser(BookLoan bookLoan) {
        if (bookLoan == null) {
            return null;
        }

        return new LoanResponse(
                bookLoan.getId(),
                bookLoan.getBook().getId(),
                bookLoan.getBook().getTitle().getValue(),
                bookLoan.getBook().getType().getValue(),
                bookLoan.getBook().getLanguage().getValue(),
                null, // 사용자 ID 숨김
                null, // 사용자 이름 숨김
                null, // 이메일 숨김
                bookLoan.getQuantity().getValue(),
                bookLoan.getLoanPeriod().getLoanDate(),
                bookLoan.getLoanPeriod().getDueDate(),
                bookLoan.getStatus().name(),
                bookLoan.getStatus().getDescription(),
                bookLoan.isOverdue(),
                bookLoan.isActive(),
                bookLoan.isReturned(),
                bookLoan.getDaysUntilDue(),
                bookLoan.getLoanPeriod().getOverdueDays(),
                bookLoan.getCreatedAt(),
                bookLoan.getUpdatedAt()
        );
    }

    // ==================== 유틸리티 메서드 ====================

    /**
     * 연체 여부를 사용자 친화적 메시지로 반환
     */
    public String getStatusMessage() {
        if (isOverdue) {
            return String.format("%d일 연체", overdueDays);
        } else if (isReturned) {
            return "반납 완료";
        } else if (isActive) {
            if (daysUntilDue <= 0) {
                return "오늘 반납";
            } else if (daysUntilDue <= 3) {
                return String.format("%d일 후 반납", daysUntilDue);
            } else {
                return "대출 중";
            }
        } else {
            return statusDescription;
        }
    }

    /**
     * 우선순위 반환 (연체 > 곧 만료 > 일반)
     */
    public Priority getPriority() {
        if (isOverdue) {
            return Priority.HIGH;
        } else if (isActive && daysUntilDue <= 3) {
            return Priority.MEDIUM;
        } else {
            return Priority.LOW;
        }
    }

    /**
     * 대출 기간 (일수) 계산
     */
    public long getLoanDuration() {
        if (loanDate != null && dueDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(loanDate, dueDate);
        }
        return 0;
    }

    /**
     * 남은 대출 기간 비율 (0.0 ~ 1.0)
     */
    public double getRemainingRatio() {
        if (!isActive || getLoanDuration() == 0) {
            return 0.0;
        }

        long totalDays = getLoanDuration();
        long remainingDays = Math.max(0, daysUntilDue);

        return Math.min(1.0, (double) remainingDays / totalDays);
    }

    /**
     * 요약 정보
     */
    public String getSummary() {
        return String.format(
                "도서: %s, 사용자: %s, 상태: %s, 반납일: %s",
                bookTitle,
                userName,
                statusDescription,
                dueDate
        );
    }

    /**
     * 우선순위 열거형
     */
    public enum Priority {
        HIGH("높음"),
        MEDIUM("보통"),
        LOW("낮음");

        private final String description;

        Priority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ==================== 검증 메서드 ====================

    /**
     * 필수 필드 유효성 검증
     */
    public boolean isValid() {
        return id != null
                && bookId != null
                && bookTitle != null && !bookTitle.trim().isEmpty()
                && userId != null
                && quantity != null && quantity > 0
                && loanDate != null
                && dueDate != null
                && status != null && !status.trim().isEmpty();
    }

    /**
     * 완전한 정보를 가지고 있는지 확인
     */
    public boolean isComplete() {
        return isValid()
                && userName != null && !userName.trim().isEmpty()
                && statusDescription != null
                && createdAt != null;
    }
}