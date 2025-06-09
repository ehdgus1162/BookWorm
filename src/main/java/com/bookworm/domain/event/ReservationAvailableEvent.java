package com.bookworm.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationAvailableEvent extends DomainEvent {
    private final Long reservationId;
    private final Long bookId;
    private final Long memberId;

    public ReservationAvailableEvent(Long reservationId, Long bookId, Long memberId, LocalDateTime occurredAt) {
        super(occurredAt);
        this.reservationId = reservationId;
        this.bookId = bookId;
        this.memberId = memberId;
    }
}