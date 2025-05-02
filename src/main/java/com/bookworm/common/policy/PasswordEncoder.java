package com.bookworm.common.policy;

import com.bookworm.domain.vo.exception.PasswordException;

/**
 * 비밀번호 암호화 및 검증을 위한 인터페이스
 *
 * 전략 패턴을 적용하여 다양한 암호화 알고리즘을 사용할 수 있게 합니다.
 * 인터페이스 분리 원칙(ISP)을 준수하여 암호화 관련 기능만 정의합니다.
 */
public interface PasswordEncoder {

    /**
     * 평문 비밀번호를 암호화하는 메서드
     *
     * @param rawPassword 평문 비밀번호
     * @return 암호화된 비밀번호
     * @throws PasswordException.PasswordEncryptionException 암호화 과정에서 오류 발생 시
     */
    String encrypt(String rawPassword) throws PasswordException.PasswordEncryptionException;

    /**
     * 평문 비밀번호와 암호화된 비밀번호가 일치하는지 검증하는 메서드
     *
     * @param rawPassword 평문 비밀번호
     * @param encodedPassword 암호화된 비밀번호
     * @return 일치 여부 (true: 일치, false: 불일치)
     * @throws PasswordException.PasswordEncryptionException 검증 과정에서 오류 발생 시
     */
    boolean matches(String rawPassword, String encodedPassword) throws PasswordException.PasswordEncryptionException;
}