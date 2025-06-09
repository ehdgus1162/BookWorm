package com.bookworm.domain.vo.bookloan;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 대출 수량 값 객체
 * - 한 번에 대출할 수 있는 수량 제한
 * - 양수만 허용
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanQuantity {

    private static final int MAX_LOAN_QUANTITY = 5; // 한 번에 최대 5권까지 대출 가능

    private Integer value;

    private LoanQuantity(Integer value) {
        this.value = value;
    }

    public static LoanQuantity of(Integer value) {
        validateQuantity(value);
        return new LoanQuantity(value);
    }

    /**
     * 대출 수량 유효성 검증
     */
    private static void validateQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("대출 수량은 필수입니다.");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("대출 수량은 1개 이상이어야 합니다.");
        }

        if (quantity > MAX_LOAN_QUANTITY) {
            throw new IllegalArgumentException("한 번에 대출할 수 있는 최대 수량은 " + MAX_LOAN_QUANTITY + "개입니다.");
        }
    }

    /**
     * 최대 대출 수량 반환
     */
    public static int getMaxLoanQuantity() {
        return MAX_LOAN_QUANTITY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanQuantity that = (LoanQuantity) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}