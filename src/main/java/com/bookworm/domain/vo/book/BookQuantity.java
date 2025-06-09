package com.bookworm.domain.vo.book;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 도서 수량 값 객체
 * - 음수 불허
 * - 최대 수량 제한
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookQuantity {

    private static final int MAX_QUANTITY = 9999;

    private Integer value;

    private BookQuantity(Integer value) {
        this.value = value;
    }

    public static BookQuantity of(Integer value) {
        validateQuantity(value);
        return new BookQuantity(value);
    }

    /**
     * 수량 유효성 검증
     */
    private static void validateQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("도서 수량은 필수입니다.");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("도서 수량은 0 이상이어야 합니다.");
        }

        if (quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("도서 수량은 " + MAX_QUANTITY + "개를 초과할 수 없습니다.");
        }
    }

    /**
     * 수량 증가
     */
    public BookQuantity increase(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("증가 수량은 양수여야 합니다.");
        }
        return BookQuantity.of(this.value + amount);
    }

    /**
     * 수량 감소
     */
    public BookQuantity decrease(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("감소 수량은 양수여야 합니다.");
        }

        if (this.value < amount) {
            throw new IllegalArgumentException("감소할 수량이 현재 수량보다 클 수 없습니다.");
        }

        return BookQuantity.of(this.value - amount);
    }

    /**
     * 재고 있는지 확인
     */
    public boolean hasStock() {
        return this.value > 0;
    }

    /**
     * 충분한 재고 있는지 확인
     */
    public boolean hasStock(Integer requiredAmount) {
        return this.value >= requiredAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookQuantity that = (BookQuantity) o;
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