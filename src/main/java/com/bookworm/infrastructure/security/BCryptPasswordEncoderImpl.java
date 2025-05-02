package com.bookworm.infrastructure.security;

import com.bookworm.common.error.ErrorMessage;
import com.bookworm.common.policy.PasswordEncoder;
import com.bookworm.domain.vo.exception.PasswordException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt 알고리즘을 사용한 PasswordEncoder 구현체
 *
 * Spring Security의 BCryptPasswordEncoder를 활용하여
 * 안전한 비밀번호 암호화와 검증을 제공합니다.
 */
@Component
public class BCryptPasswordEncoderImpl implements PasswordEncoder {

    private final BCryptPasswordEncoder encoder;

    /**
     * 기본 강도(10)의 BCrypt 알고리즘을 사용하는 생성자
     */
    public BCryptPasswordEncoderImpl() {
        this.encoder = new BCryptPasswordEncoder();
    }

    /**
     * 지정된 강도의 BCrypt 알고리즘을 사용하는 생성자
     *
     * @param strength 암호화 강도 (4~31, 높을수록 보안은 강화되지만 시간이 오래 걸림)
     */
    public BCryptPasswordEncoderImpl(int strength) {
        this.encoder = new BCryptPasswordEncoder(strength);
    }


    /**
     * 평문 비밀번호를 BCrypt 알고리즘으로 암호화
     *
     * @param rawPassword 평문 비밀번호
     * @return BCrypt로 암호화된 비밀번호
     * @throws PasswordException.PasswordEncryptionException 암호화 과정에서 오류 발생 시
     */
    @Override
    public String encrypt(String rawPassword)
            throws PasswordException.PasswordEncryptionException {

        try {
            return encoder.encode(rawPassword);
        } catch (Exception e) {
            throw new PasswordException.PasswordEncryptionException(ErrorMessage.Password.ENCRYPTION_FAILED, e);
        }
    }

    /**
     * 평문 비밀번호와 암호화된 비밀번호가 일치하는지 검증
     *
     * @param rawPassword 평문 비밀번호
     * @param encodedPassword BCrypt로 암호화된 비밀번호
     * @return 일치 여부
     * @throws PasswordException.PasswordEncryptionException 검증 과정에서 오류 발생 시
     */
    @Override
    public boolean matches(String rawPassword, String encodedPassword) throws PasswordException.PasswordEncryptionException {
        try {
            return encoder.matches(rawPassword, encodedPassword);
        } catch (Exception e) {
            throw new PasswordException.PasswordEncryptionException(ErrorMessage.Password.MATCH_FAILED, e);
        }
    }
}
