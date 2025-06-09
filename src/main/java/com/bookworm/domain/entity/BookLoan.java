package com.bookworm.domain.entity;

import com.bookworm.domain.common.AuditableBaseEntity;
import com.bookworm.domain.common.TimeProvider;
import com.bookworm.domain.constant.LoanStatus;
import com.bookworm.domain.exception.LoanBusinessException;
import com.bookworm.domain.vo.bookloan.LoanPeriod;
import com.bookworm.domain.vo.bookloan.LoanQuantity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 도서 대출 엔티티
 * - 도서와 사용자 간의 대출 관계 관리
 * - 대출 기간 및 반납 관리
 * - 대출 상태별 비즈니스 로직 포함
 */
@Entity
@Table(name = "book_loans", indexes = {
        @Index(name = "idx_loan_user", columnList = "user_id"),
        @Index(name = "idx_loan_book", columnList = "book_id"),
        @Index(name = "idx_loan_status", columnList = "status"),
        @Index(name = "idx_loan_due_date", columnList = "due_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class BookLoan extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 대출한 도서
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /**
     * 대출한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "quantity", nullable = false))
    private LoanQuantity quantity;

    @Embedded
    private LoanPeriod loanPeriod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Version
    private Long version;

    /**
     * 새로운 대출 생성
     */
    public static BookLoan create(Book book, User user, LoanQuantity quantity, LoanPeriod loanPeriod) {
        validateCreateInputs(book, user, quantity, loanPeriod);
        validateLoanConditions(book, user, quantity);

        return BookLoan.of()
                .book(book)
                .user(user)
                .quantity(quantity)
                .loanPeriod(loanPeriod)
                .status(LoanStatus.ACTIVE)
                .build();
    }

    /**
     * 기본 대출 기간으로 새로운 대출 생성
     */
    public static BookLoan createWithDefaultPeriod(Book book, User user, LoanQuantity quantity) {
        return create(book, user, quantity, LoanPeriod.createDefault());
    }

    /**
     * 대출 생성 시 입력값 검증
     */
    private static void validateCreateInputs(Book book, User user, LoanQuantity quantity, LoanPeriod loanPeriod) {
        if (book == null) {
            throw new LoanBusinessException("대출할 도서는 필수입니다.");
        }
        if (user == null) {
            throw new LoanBusinessException("대출자 정보는 필수입니다.");
        }
        if (quantity == null) {
            throw new LoanBusinessException("대출 수량은 필수입니다.");
        }
        if (loanPeriod == null) {
            throw new LoanBusinessException("대출 기간은 필수입니다.");
        }
    }

    /**
     * 대출 조건 검증
     */
    private static void validateLoanConditions(Book book, User user, LoanQuantity quantity) {
        if (!user.isActive()) {
            throw new LoanBusinessException("비활성화된 사용자는 도서를 대출할 수 없습니다.");
        }

        if (!book.canBorrow(quantity.getValue())) {
            throw new LoanBusinessException("해당 도서는 현재 대출할 수 없습니다.");
        }
    }

    // 핵심 비즈니스 메서드

    /**
     * 대출 실행 (도서 재고 감소)
     */
    public void executeLoan() {
        if (this.status != LoanStatus.ACTIVE) {
            throw new LoanBusinessException("활성 상태의 대출만 실행할 수 있습니다.");
        }

        // 도서 재고 감소
        this.book.borrowStock(this.quantity.getValue());
    }

    /**
     * 도서 반납
     */
    public void returnBook(TimeProvider timeProvider) {
        if (this.status != LoanStatus.ACTIVE) {
            throw new LoanBusinessException("활성 상태의 대출만 반납할 수 있습니다.");
        }

        // 도서 재고 증가
        this.book.returnStock(this.quantity.getValue());

        // 대출 상태를 반납 완료로 변경
        this.status = LoanStatus.RETURNED;
    }

    /**
     * 대출 연장
     */
    public void extendLoan(int days) {
        if (this.status != LoanStatus.ACTIVE) {
            throw new LoanBusinessException("활성 상태의 대출만 연장할 수 있습니다.");
        }

        if (isOverdue()) {
            throw new LoanBusinessException("연체된 대출은 연장할 수 없습니다.");
        }

        this.loanPeriod = this.loanPeriod.extend(days);
    }

    /**
     * 대출 취소 (대출 실행 전에만 가능)
     */
    public void cancelLoan() {
        if (this.status != LoanStatus.ACTIVE) {
            throw new LoanBusinessException("활성 상태의 대출만 취소할 수 있습니다.");
        }

        this.status = LoanStatus.CANCELLED;
    }

    /**
     * 상태 변경
     */
    public void changeStatus(LoanStatus newStatus) {
        if (newStatus == null) {
            throw new LoanBusinessException("대출 상태는 필수입니다.");
        }
        this.status = newStatus;
    }

    /**
     * 연체 여부 확인
     */
    public boolean isOverdue() {
        return this.status == LoanStatus.ACTIVE &&
                LocalDate.now().isAfter(this.loanPeriod.getDueDate());
    }

    /**
     * 반납 예정일까지 남은 일수
     */
    public long getDaysUntilDue() {
        return this.loanPeriod.getDaysUntilDue();
    }

    /**
     * 활성 대출 여부 확인
     */
    public boolean isActive() {
        return this.status == LoanStatus.ACTIVE;
    }

    /**
     * 반납 완료 여부 확인
     */
    public boolean isReturned() {
        return this.status == LoanStatus.RETURNED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BookLoan bookLoan = (BookLoan) o;

        if (id == null || bookLoan.id == null) {
            return false;
        }

        return Objects.equals(id, bookLoan.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("BookLoan{id=%d, book='%s', user='%s', quantity=%s, dueDate=%s, status=%s}",
                id,
                book != null ? book.getTitle() : "null",
                user != null ? user.getFullName() : "null",
                quantity,
                loanPeriod != null ? loanPeriod.getDueDate() : "null",
                status);
    }
}