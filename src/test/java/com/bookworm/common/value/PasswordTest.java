package com.bookworm.common.value;

import com.bookworm.domain.vo.value.Password;
import com.bookworm.common.error.ErrorMessage;
import com.bookworm.domain.vo.exception.PasswordException;
import com.bookworm.domain.vo.validator.PasswordValidator;
import com.bookworm.domain.vo.validator.PasswordValidatorImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {

    private static final PasswordValidator validator = new PasswordValidatorImpl();

    @Nested
    @DisplayName("비밀번호 생성 테스트")
    class CreatePasswordTest {

        @Test
        @DisplayName("유효한 비밀번호로 객체 생성 성공")
        void createPasswordWithValidFormat() {
            // Given
            String validPassword = "Password123!";

            // When
            Password password = new Password(validPassword);

            // Then
            assertThat(password.getValue()).isEqualTo(validPassword);
            assertThat(password.isEncrypted()).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("비밀번호가 null이거나 빈 값이면 예외 발생")
        void throwExceptionForNullOrEmptyPassword(String invalidPassword) {
            // When & Then
            assertThatThrownBy(() -> new Password(invalidPassword))
                    .isInstanceOf(PasswordException.EmptyPasswordException.class)
                    .hasMessageContaining(ErrorMessage.Password.REQUIRED);
        }

        @Test
        @DisplayName("비밀번호 길이가 8자 미만이면 예외 발생")
        void throwExceptionForShortPassword() {
            // Given
            String shortPassword = "Pass1!";

            // When & Then
            assertThatThrownBy(() -> new Password(shortPassword))
                    .isInstanceOf(PasswordException.InvalidPasswordFormatException.class)
                    .hasMessageContaining(ErrorMessage.Password.LENGTH);
        }

        @Test
        @DisplayName("비밀번호에 대문자가 없으면 예외 발생")
        void throwExceptionForPasswordWithoutUppercase() {
            // Given
            String noUppercasePassword = "password123!";

            // When & Then
            assertThatThrownBy(() -> new Password(noUppercasePassword))
                    .isInstanceOf(PasswordException.InvalidPasswordFormatException.class)
                    .hasMessageContaining(ErrorMessage.Password.UPPERCASE);
        }

        @Test
        @DisplayName("비밀번호에 숫자가 없으면 예외 발생")
        void throwExceptionForPasswordWithoutDigit() {
            // Given
            String noDigitPassword = "Password!";

            // When & Then
            assertThatThrownBy(() -> new Password(noDigitPassword))
                    .isInstanceOf(PasswordException.InvalidPasswordFormatException.class)
                    .hasMessageContaining(ErrorMessage.Password.DIGIT);
        }

        @Test
        @DisplayName("비밀번호에 특수문자가 없으면 예외 발생")
        void throwExceptionForPasswordWithoutSpecialChar() {
            // Given
            String noSpecialCharPassword = "Password123";

            // When & Then
            assertThatThrownBy(() -> new Password(noSpecialCharPassword))
                    .isInstanceOf(PasswordException.InvalidPasswordFormatException.class)
                    .hasMessageContaining(ErrorMessage.Password.SPECIAL_CHAR);
        }
    }

    @Nested
    @DisplayName("암호화된 비밀번호 테스트")
    class EncryptedPasswordTest {

        @Test
        @DisplayName("암호화된 패스워드 생성 검증")
        void createEncryptedPassword() {
            // Given
            String encryptedValue = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"; // 예시 해시값

            // When
            Password password = Password.ofEncrypted(encryptedValue);

            // Then
            assertThat(password.getValue()).isEqualTo(encryptedValue);
            assertThat(password.isEncrypted()).isTrue();
        }

        @Test
        @DisplayName("암호화된 패스워드 생성 시 null 값이면 예외 발생")
        void throwExceptionForNullEncryptedPassword() {
            // Given
            String nullEncryptedPassword = null;

            // When & Then
            assertThatThrownBy(() -> Password.ofEncrypted(nullEncryptedPassword))
                    .isInstanceOf(PasswordException.PasswordEncryptionException.class)
                    .hasMessageContaining(ErrorMessage.Password.ENCRYPT_REQUIRED);
        }

        @Test
        @DisplayName("암호화된 패스워드 생성 시 빈 문자열이면 예외 발생")
        void throwExceptionForBlankEncryptedPassword() {
            // Given
            String blankEncryptedPassword = "   ";

            // When & Then
            assertThatThrownBy(() -> Password.ofEncrypted(blankEncryptedPassword))
                    .isInstanceOf(PasswordException.PasswordEncryptionException.class)
                    .hasMessageContaining(ErrorMessage.Password.ENCRYPT_REQUIRED);
        }

        @Test
        @DisplayName("잘못된 형식의 암호화 문자열이면 예외 발생")
        void invalidFormatThrowsException() {
            // Given
            String invalid = "not-bcrypt";

            // When & Then
            assertThatThrownBy(() -> Password.ofEncrypted(invalid))
                    .isInstanceOf(PasswordException.PasswordEncryptionException.class)
                    .hasMessageContaining(ErrorMessage.Password.ENCRYPT_INVALID);
        }
    }

    @Nested
    @DisplayName("비밀번호 문자열 표현 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString 호출 시 평문 비밀번호는 노출되지 않음")
        void toStringHidesRawPassword() {
            // Given
            String validPassword = "Password123!";

            // When
            Password password = new Password(validPassword);

            // Then
            assertFalse(password.toString().contains(validPassword));
        }

        @Test
        @DisplayName("toString 호출 시 암호화된 비밀번호는 노출되지 않음")
        void toStringHidesEncryptedPassword() {
            // Given
            String encryptedValue = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG";

            // When
            Password password = Password.ofEncrypted(encryptedValue);

            // Then
            assertFalse(password.toString().contains(encryptedValue));
        }
    }
}