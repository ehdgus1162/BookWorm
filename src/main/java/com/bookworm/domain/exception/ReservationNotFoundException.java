package com.bookworm.domain.exception;

public class ReservationNotFoundException extends LibraryBusinessException {
    public ReservationNotFoundException(Long reservationId) {
        super("RESERVATION_NOT_FOUND", "예약 정보를 찾을 수 없습니다. ID: " + reservationId);
    }
}