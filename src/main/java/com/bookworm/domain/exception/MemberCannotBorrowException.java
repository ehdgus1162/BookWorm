package com.bookworm.domain.exception;

public class MemberCannotBorrowException extends LibraryBusinessException {
    public MemberCannotBorrowException(String memberId, String reason) {
        super("MEMBER_CANNOT_BORROW",
                String.format("회원 %s는 대출할 수 없습니다. 사유: %s", memberId, reason));
    }
}