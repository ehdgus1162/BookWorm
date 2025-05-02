package com.bookworm.common.value;

import com.bookworm.domain.vo.value.LastName;
import com.bookworm.common.error.ErrorMessage;
import com.bookworm.domain.vo.exception.NameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LastName 값 객체 테스트")
class LastNameTest {

    @Nested
    @DisplayName("성(LastName) 생성 테스트")
    class CreateLastNameTest {

        @Test
        @DisplayName("유효한 성으로 객체 생성 성공")
        void createLastNameWithValidName() {
            // Given
            String validName = "홍";

            // When
            LastName lastName = new LastName(validName);

            // Then
            assertThat(lastName.getValue()).isEqualTo(validName);
        }

        @Test
        @DisplayName("팩토리 메서드로 객체 생성 성공")
        void createLastNameWithFactoryMethod() {
            // Given
            String validName = "김";

            // When
            LastName lastName = LastName.of(validName);

            // Then
            assertThat(lastName.getValue()).isEqualTo(validName);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("성이 null이거나 빈 값이면 예외 발생")
        void throwExceptionForNullOrEmptyName(String invalidName) {
            // When & Then
            assertThatThrownBy(() -> new LastName(invalidName))
                    .isInstanceOf(NameException.EmptyNameException.class)
                    .hasMessageContaining(ErrorMessage.Name.EMPTY);
        }

        @Test
        @DisplayName("성 길이가 최대 길이보다 길면 예외 발생")
        void throwExceptionForTooLongName() {
            // Given
            String tooLongName = "a".repeat(22);

            // When & Then
            assertThatThrownBy(() -> new LastName(tooLongName))
                    .isInstanceOf(NameException.InvalidNameLengthException.class)
                    .hasMessageContaining(ErrorMessage.Name.LAST_NAME_LENGTH, 1, 20);
        }
    }

    @Nested
    @DisplayName("성(LastName) 동등성 테스트")
    class LastNameEqualityTest {

        @Test
        @DisplayName("동일한 값을 가진 두 성 객체는 equals 비교 시 true를 반환한다")
        void sameValueEqualityTest() {
            // Given
            LastName name1 = new LastName("김");
            LastName name2 = new LastName("김");

            // When & Then
            assertEquals(name1, name2);
            assertEquals(name1.hashCode(), name2.hashCode());
        }

        @Test
        @DisplayName("서로 다른 값을 가진 두 성 객체는 equals 비교 시 false를 반환한다")
        void differentValueEqualityTest() {
            // Given
            LastName name1 = new LastName("김");
            LastName name2 = new LastName("이");

            // When & Then
            assertNotEquals(name1, name2);
            assertNotEquals(name1.hashCode(), name2.hashCode());
        }
    }

    @Test
    @DisplayName("toString 메서드는 성 값을 반환한다")
    void toStringTest() {
        // Given
        String name = "김";
        LastName lastName = new LastName(name);

        // When
        String result = lastName.toString();

        // Then
        assertEquals(name, result);
    }
}