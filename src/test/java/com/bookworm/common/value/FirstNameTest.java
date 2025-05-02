package com.bookworm.common.value;

import com.bookworm.domain.vo.value.FirstName;
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

@DisplayName("FirstName 값 객체 테스트")
class FirstNameTest {

    @Nested
    @DisplayName("이름(FirstName) 생성 테스트")
    class CreateFirstNameTest {

        @Test
        @DisplayName("유효한 이름으로 객체 생성 성공")
        void createFirstNameWithValidName() {
            // Given
            String validName = "길동";

            // When
            FirstName firstName = new FirstName(validName);

            // Then
            assertThat(firstName.getValue()).isEqualTo(validName);
        }

        @Test
        @DisplayName("팩토리 메서드로 객체 생성 성공")
        void createFirstNameWithFactoryMethod() {
            // Given
            String validName = "철수";

            // When
            FirstName firstName = FirstName.of(validName);

            // Then
            assertThat(firstName.getValue()).isEqualTo(validName);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("이름이 null이거나 빈 값이면 예외 발생")
        void throwExceptionForNullOrEmptyName(String invalidName) {
            // When & Then
            assertThatThrownBy(() -> new FirstName(invalidName))
                    .isInstanceOf(NameException.EmptyNameException.class)
                    .hasMessageContaining(ErrorMessage.Name.EMPTY);
        }

        @Test
        @DisplayName("이름 길이가 최소 길이보다 짧으면 예외 발생")
        void throwExceptionForTooShortName() {
            // Given
            String tooShortName = "김";

            // When & Then
            assertThatThrownBy(() -> new FirstName(tooShortName))
                    .isInstanceOf(NameException.InvalidNameLengthException.class)
                    .hasMessageContaining(ErrorMessage.Name.FIRST_NAME_LENGTH);
        }

        @Test
        @DisplayName("이름 길이가 최대 길이보다 길면 예외 발생")
        void throwExceptionForTooLongName() {
            // Given
            String tooLongName = "a".repeat(31);

            // When & Then
            assertThatThrownBy(() -> new FirstName(tooLongName))
                    .isInstanceOf(NameException.InvalidNameLengthException.class)
                    .hasMessageContaining(ErrorMessage.Name.FIRST_NAME_LENGTH);
        }
    }

    @Nested
    @DisplayName("이름(FirstName) 동등성 테스트")
    class FirstNameEqualityTest {

        @Test
        @DisplayName("동일한 값을 가진 두 이름 객체는 equals 비교 시 true를 반환한다")
        void sameValueEqualityTest() {
            // Given
            FirstName name1 = new FirstName("철수");
            FirstName name2 = new FirstName("철수");

            // When & Then
            assertEquals(name1, name2);
            assertEquals(name1.hashCode(), name2.hashCode());
        }

        @Test
        @DisplayName("서로 다른 값을 가진 두 이름 객체는 equals 비교 시 false를 반환한다")
        void differentValueEqualityTest() {
            // Given
            FirstName name1 = new FirstName("철수");
            FirstName name2 = new FirstName("영희");

            // When & Then
            assertNotEquals(name1, name2);
            assertNotEquals(name1.hashCode(), name2.hashCode());
        }
    }

    @Test
    @DisplayName("toString 메서드는 이름 값을 반환한다")
    void toStringTest() {
        // Given
        String name = "철수";
        FirstName firstName = new FirstName(name);

        // When
        String result = firstName.toString();

        // Then
        assertEquals(name, result);
    }
}
