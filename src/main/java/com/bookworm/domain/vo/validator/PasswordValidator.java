package com.bookworm.domain.vo.validator;

import com.bookworm.domain.vo.exception.PasswordException;

/**
 * 비밀번호 검증을 위한 인터페이스
 *
 * 전략 패턴을 적용하여 다양한 검증을 사용할 수 있게 합니다.
 * 인터페이스 분리 원칙(ISP)을 준수하여 유효성 관련 기능만 정의합니다.
 */
public interface PasswordValidator {


    void validate(String password) throws PasswordException;

    boolean isValid(String password);
}
