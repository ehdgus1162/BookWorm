package com.bookworm.domain.vo.bookloan;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * 대출 기간 값 객체
 * - 대출 시작일과 반납 예정일 관리
 * - 대출 기간 연장 기능
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanPeriod {

    private static final int DEFAULT_LOAN_DAYS = 14; // 기본 대출 기간: 14일
    private static final int MAX_EXTENSION_DAYS = 14; // 최대 연장 가능 일수

    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    private LoanPeriod(LocalDate loanDate, LocalDate dueDate) {
        this.loanDate = loanDate;
        this.dueDate = dueDate;
    }

    /**
     * 기본 대출 기간으로 생성 (오늘부터 14일)
     */
    public static LoanPeriod createDefault() {
        LocalDate today = LocalDate.now();
        LocalDate defaultDueDate = today.plusDays(DEFAULT_LOAN_DAYS);
        return new LoanPeriod(today, defaultDueDate);
    }

    /**
     * 사용자 지정 대출 기간으로 생성
     */
    public static LoanPeriod of(LocalDate loanDate, LocalDate dueDate) {
        validatePeriod(loanDate, dueDate);
        return new LoanPeriod(loanDate, dueDate);
    }

    /**
     * 특정 일수로 대출 기간 생성
     */
    public static LoanPeriod ofDays(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("대출 기간은 1일 이상이어야 합니다.");
        }

        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(days);
        return new LoanPeriod(today, dueDate);
    }

    /**
     * 대출 기간 유효성 검증
     */
    private static void validatePeriod(LocalDate loanDate, LocalDate dueDate) {
        if (loanDate == null) {
            throw new IllegalArgumentException("대출 시작일은 필수입니다.");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("반납 예정일은 필수입니다.");
        }
        if (loanDate.isAfter(dueDate)) {
            throw new IllegalArgumentException("반납 예정일은 대출 시작일보다 늦어야 합니다.");
        }
        if (loanDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("대출 시작일은 과거일 수 없습니다.");
        }
    }

    /**
     * 대출 기간 연장
     */
    public LoanPeriod extend(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("연장 일수는 1일 이상이어야 합니다.");
        }
        if (days > MAX_EXTENSION_DAYS) {
            throw new IllegalArgumentException("최대 " + MAX_EXTENSION_DAYS + "일까지만 연장 가능합니다.");
        }

        LocalDate newDueDate = this.dueDate.plusDays(days);
        return new LoanPeriod(this.loanDate, newDueDate);
    }

    /**
     * 반납 예정일까지 남은 일수
     */
    public long getDaysUntilDue() {
        return ChronoUnit.DAYS.between(LocalDate.now(), this.dueDate);
    }

    /**
     * 대출 기간 (총 일수)
     */
    public long getTotalLoanDays() {
        return ChronoUnit.DAYS.between(this.loanDate, this.dueDate);
    }

    /**
     * 연체 여부 확인
     */
    public boolean isOverdue() {
        return LocalDate.now().isAfter(this.dueDate);
    }

    /**
     * 연체 일수 계산
     */
    public long getOverdueDays() {
        if (!isOverdue()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(this.dueDate, LocalDate.now());
    }

    /**
     * 기본 대출 기간 반환
     */
    public static int getDefaultLoanDays() {
        return DEFAULT_LOAN_DAYS;
    }

    /**
     * 최대 연장 가능 일수 반환
     */
    public static int getMaxExtensionDays() {
        return MAX_EXTENSION_DAYS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanPeriod that = (LoanPeriod) o;
        return Objects.equals(loanDate, that.loanDate) &&
                Objects.equals(dueDate, that.dueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loanDate, dueDate);
    }

    @Override
    public String toString() {
        return String.format("LoanPeriod{loanDate=%s, dueDate=%s}", loanDate, dueDate);
    }
}