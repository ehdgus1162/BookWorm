package com.bookworm.domain.exception;

public class BookNotAvailableException extends LibraryBusinessException {
    public BookNotAvailableException(String barcode) {
        super("BOOK_NOT_AVAILABLE", "대출할 수 없는 도서입니다. 바코드: " + barcode);
    }
}