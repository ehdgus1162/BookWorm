package com.bookworm.application.service.Loan;

/**
 * 대출을 찾을 수 없을 때 발생하는 예외
 */
public class LoanNotFoundException extends RuntimeException {

    public LoanNotFoundException(String message) {
        super(message);
    }

    public LoanNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}