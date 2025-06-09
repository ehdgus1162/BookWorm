package com.bookworm.domain.constant;

import lombok.Getter;

/**
 * 도서 상태
 * - 도서의 이용 가능 여부 관리
 * - 각 상태별 비즈니스 규칙 정의
 */
@Getter
public enum BookStatus {
    AVAILABLE("이용 가능", "대출 및 예약이 가능한 상태"),
    BORROWED("대출 중", "모든 재고가 대출되어 이용 불가능한 상태"),
    RESERVED("예약됨", "예약이 있어 일반 대출은 불가능한 상태"),
    MAINTENANCE("정비 중", "도서 정비나 수리로 인해 일시적으로 이용 불가능한 상태"),
    LOST("분실", "도서가 분실되어 이용할 수 없는 상태"),
    DAMAGED("파손", "도서가 파손되어 이용할 수 없는 상태");

    private final String description;
    private final String detailDescription;

    BookStatus(String description, String detailDescription) {
        this.description = description;
        this.detailDescription = detailDescription;
    }

    /**
     * 대출 가능한 상태인지 확인
     */
    public boolean canBorrow() {
        return this == AVAILABLE;
    }

    /**
     * 예약 가능한 상태인지 확인
     */
    public boolean canReserve() {
        return this == AVAILABLE || this == BORROWED;
    }

    /**
     * 정상적인 운영 상태인지 확인 (분실, 파손 등이 아닌 상태)
     */
    public boolean isOperational() {
        return this == AVAILABLE || this == BORROWED || this == RESERVED || this == MAINTENANCE;
    }

    /**
     * 문제가 있는 상태인지 확인 (분실, 파손 등)
     */
    public boolean isProblematic() {
        return this == LOST || this == DAMAGED;
    }
}
