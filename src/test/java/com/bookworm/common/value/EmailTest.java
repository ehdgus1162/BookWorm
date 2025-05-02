package com.bookworm.common.value;

import com.bookworm.domain.vo.value.Email;
import com.bookworm.domain.vo.exception.EmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Nested
    @DisplayName("이메일 생성 테스트")
    class CreateEmailTest {

        @Test
        @DisplayName("유효한 이메일 주소로 객체 생성 성공")
        void createEmailSuccess() {
            // Given
            String validEmail = "admin@bookworm.com";

            // When
            Email email = Email.of(validEmail);

            // Then
            assertEquals(validEmail, email.getValue());
        }

        @Test
        @DisplayName("빈 이메일 주소로 객체 생성 실패")
        void createEmailWithEmptyValueFails() {
            // Given
            String emptyEmail = "";

            // When & Then
            assertThrows(EmailException.EmptyEmailException.class, () -> {
                Email.of(emptyEmail);
            });
        }
    }
}
