package com.bookworm.common.value;

import com.bookworm.domain.vo.value.FirstName;
import com.bookworm.domain.vo.value.FullName;
import com.bookworm.domain.vo.value.LastName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FullName 값 객체 테스트")
class FullNameTest {

    @Nested
    @DisplayName("전체 이름(FullName) 생성 테스트")
    class CreateFullNameTest {

        @Test
        @DisplayName("성과 이름으로 객체 생성 성공")
        void createFullNameWithFirstAndLastName() {
            // Given
            LastName lastName = new LastName("김");
            FirstName firstName = new FirstName("철수");

            // When
            FullName fullName = new FullName(lastName, firstName);

            // Then
            assertThat(fullName.getLastName()).isEqualTo(lastName);
            assertThat(fullName.getFirstName()).isEqualTo(firstName);
        }

        @Test
        @DisplayName("팩토리 메서드로 객체 생성 성공")
        void createFullNameWithFactoryMethod() {
            // Given
            String lastName = "김";
            String firstName = "철수";

            // When
            FullName fullName = FullName.of(lastName, firstName);

            // Then
            assertThat(fullName.getLastName().getValue()).isEqualTo(lastName);
            assertThat(fullName.getFirstName().getValue()).isEqualTo(firstName);
        }
    }

    @Nested
    @DisplayName("전체 이름(FullName) 동등성 테스트")
    class FullNameEqualityTest {

        @Test
        @DisplayName("동일한 값을 가진 두 전체 이름 객체는 equals 비교 시 true를 반환한다")
        void sameValueEqualityTest() {
            // Given
            FullName name1 = FullName.of("김", "철수");
            FullName name2 = FullName.of("김", "철수");

            // When & Then
            assertEquals(name1, name2);
            assertEquals(name1.hashCode(), name2.hashCode());
        }

        @Test
        @DisplayName("서로 다른 성을 가진 두 전체 이름 객체는 equals 비교 시 false를 반환한다")
        void differentLastNameEqualityTest() {
            // Given
            FullName name1 = FullName.of("김", "철수");
            FullName name2 = FullName.of("이", "철수");

            // When & Then
            assertNotEquals(name1, name2);
            assertNotEquals(name1.hashCode(), name2.hashCode());
        }

        @Test
        @DisplayName("서로 다른 이름을 가진 두 전체 이름 객체는 equals 비교 시 false를 반환한다")
        void differentFirstNameEqualityTest() {
            // Given
            FullName name1 = FullName.of("김", "철수");
            FullName name2 = FullName.of("김", "영희");

            // When & Then
            assertNotEquals(name1, name2);
            assertNotEquals(name1.hashCode(), name2.hashCode());
        }
    }

    @Test
    @DisplayName("toString 메서드는 성과 이름을 결합한 값을 반환한다")
    void toStringTest() {
        // Given
        FullName fullName = FullName.of("김", "철수");

        // When
        String result = fullName.toString();

        // Then
        assertEquals("김철수", result);
    }

    @Test
    @DisplayName("getFullName 메서드는 성과 이름을 결합한 값을 반환한다")
    void getFullNameTest() {
        // Given
        FullName fullName = FullName.of("김", "철수");

        // When
        String result = fullName.getFullName();

        // Then
        assertEquals("김철수", result);
    }
}

