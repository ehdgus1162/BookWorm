package com.bookworm.application.service.Loan;

/**
 * 대출 정책 위반 예외
 */
public class LoanPolicyViolationException extends RuntimeException {

    public LoanPolicyViolationException(String message) {
        super(message);
    }

    public LoanPolicyViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}