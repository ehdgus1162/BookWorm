package com.bookworm.domain.vo.value;

import com.bookworm.common.error.ErrorMessage;
import com.bookworm.domain.vo.exception.PasswordException;
import com.bookworm.domain.vo.validator.PasswordValidator;
import com.bookworm.domain.vo.validator.PasswordValidatorImpl;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 비밀번호 값 객체
 * 비밀번호 형식 유효성 검증 및 비밀번호 캡슐화
 */
@Embeddable
@Getter
public class Password implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String value;
    private final boolean encrypted;

    private static final PasswordValidator validator = new PasswordValidatorImpl();

    /**
     * JPA를 위한 기본 생성자
     * 직접 호출하지 말고 팩토리 메서드 사용
     */
    protected Password() {
        this.value = null;
        this.encrypted = false;
    }

    /**
     * 평문 비밀번호로 객체를 생성하는 생성자
     *
     * @param plainPassword 평문 비밀번호
     * @throws PasswordException.EmptyPasswordException 비밀번호가 null이거나 빈 값인 경우
     */
    public Password(String plainPassword) {
        validator.validate(plainPassword);
        this.value = plainPassword;
        this.encrypted = false;
    }

    /**
     * 내부용 생성자 - 암호화 여부 설정 가능
     */
    private Password(String value, boolean encrypted) {
        this.value = value;
        this.encrypted = encrypted;
    }

    /**
     * 암호화된 비밀번호 생성 팩토리 메서드
     *
     * @param encryptedValue 암호화된 비밀번호 값
     * @return 암호화된 비밀번호 객체
     * @throws PasswordException.PasswordEncryptionException 암호화된 값이 유효하지 않을 경우
     */
    public static Password ofEncrypted(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            throw new PasswordException.PasswordEncryptionException(ErrorMessage.Password.ENCRYPT_REQUIRED);
        }
        if (!encryptedValue.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$")) {
            throw new PasswordException.PasswordEncryptionException(ErrorMessage.Password.ENCRYPT_INVALID);
        }
        return new Password(encryptedValue, true);
    }

    /**
     * 평문 비밀번호 생성 팩토리 메서드
     *
     * @param plainPassword 평문 비밀번호
     * @return 평문 비밀번호 객체
     */
    public static Password of(String plainPassword) {
        validator.validate(plainPassword);
        return new Password(plainPassword, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return encrypted == password.encrypted && Objects.equals(value, password.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, encrypted);
    }

    @Override
    public String toString() {
        return encrypted ? "[ENCRYPTED]" : "[RAW]";
    }
}