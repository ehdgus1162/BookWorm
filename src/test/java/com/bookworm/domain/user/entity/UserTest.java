package com.bookworm.domain.user.entity;

import com.bookworm.domain.user.constant.Role;
import com.bookworm.domain.user.constant.UserStatus;
import com.bookworm.domain.vo.exception.EmailException;
import com.bookworm.domain.vo.exception.NameException;
import com.bookworm.domain.vo.exception.PasswordException;
import com.bookworm.domain.vo.value.Email;
import com.bookworm.domain.vo.value.FullName;
import com.bookworm.domain.vo.value.Password;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Nested
    @DisplayName("사용자 생성 테스트")
    class UserCreationTest {

        @Test
        @DisplayName("유효한 값으로 사용자를 생성할 수 있다")
        void createUserWithValidValues() {
            // given
            String lastName = "김";
            String firstName = "철수";
            String email = "test@example.com";
            String password = "Password1!";
            Role role = Role.USER;

            // when
            User user = User.of(lastName, firstName, email, password, role);

            // then
            assertNotNull(user);
            assertEquals(email, user.getEmail().getValue());
            assertEquals(lastName + firstName, user.getName().getFullName());
            assertEquals(UserStatus.ACTIVE, user.getStatus());
            assertFalse(user.getPassword().isEncrypted());
            assertEquals(password, user.getPassword().getValue());
            assertEquals(Role.USER, user.getRole());
        }

        @Test
        @DisplayName("사용자 로그인 시간이 정상적으로 기록된다")
        void recordLoginTime() {
            // given
            User user = User.of("김", "철수", "test@example.com", "Password1!", Role.USER);
            LocalDateTime loginTime = LocalDateTime.now();

            // when
            user.markLogin(loginTime);

            // then
            assertEquals(loginTime, user.getLastLoginAt());
            System.out.println(user.getLastLoginAt() + " 유저의 로그인 시간");
        }
    }

    @Nested
    @DisplayName("값 객체 유효성 검증 테스트")
    class ValueObjectValidationTest {

        @Nested
        @DisplayName("이메일 유효성 검증")
        class EmailValidationTest {

            @Test
            @DisplayName("유효하지 않은 이메일 형식으로 사용자를 생성할 수 없다")
            void cannotCreateUserWithInvalidEmail() {
                // given
                String lastName = "김";
                String firstName = "철수";
                String invalidEmail = "invalid-email";
                String password = "Password1!";

                // when & then
                assertThrows(EmailException.InvalidEmailFormatException.class, () -> {
                    User.of(lastName, firstName, invalidEmail, password, Role.USER);
                });
            }

            @Test
            @DisplayName("빈 이메일로 사용자를 생성할 수 없다")
            void cannotCreateUserWithEmptyEmail() {
                // given
                String lastName = "김";
                String firstName = "철수";
                String emptyEmail = "";
                String password = "Password1!";

                // when & then
                assertThrows(EmailException.EmptyEmailException.class, () -> {
                    User.of(lastName, firstName, emptyEmail, password, Role.USER);
                });
            }
        }

        @Nested
        @DisplayName("이름 유효성 검증")
        class NameValidationTest {

            @Test
            @DisplayName("유효하지 않은 길이의 이름으로 사용자를 생성할 수 없다")
            void cannotCreateUserWithInvalidNameLength() {
                // given
                String lastName = "김";
                String tooShortFirstName = "김";  // 2글자 미만
                String email = "test@example.com";
                String password = "Password1!";

                // when & then
                assertThrows(NameException.InvalidNameLengthException.class, () -> {
                    User.of(lastName, tooShortFirstName, email, password, Role.USER);
                });
            }

            @Test
            @DisplayName("빈 이름으로 사용자를 생성할 수 없다")
            void cannotCreateUserWithEmptyName() {
                // given
                String lastName = "김";
                String emptyFirstName = "";
                String email = "test@example.com";
                String password = "Password1!";

                // when & then
                assertThrows(NameException.EmptyNameException.class, () -> {
                    User.of(lastName, emptyFirstName, email, password, Role.USER);
                });
            }

            @Test
            @DisplayName("빈 성으로 사용자를 생성할 수 없다")
            void cannotCreateUserWithEmptyLastName() {
                // given
                String emptyLastName = "";
                String firstName = "철수";
                String email = "test@example.com";
                String password = "Password1!";

                // when & then
                assertThrows(NameException.EmptyNameException.class, () -> {
                    User.of(emptyLastName, firstName, email, password, Role.USER);
                });
            }
        }

        @Nested
        @DisplayName("비밀번호 유효성 검증")
        class PasswordValidationTest {

            @Test
            @DisplayName("비밀번호는 최소 8글자 이상이어야 한다")
            void passwordMustBeAtLeastEightCharacters() {
                // given
                String lastName = "김";
                String firstName = "철수";
                String email = "test@example.com";
                String shortPassword = "Pass1!"; // 8글자 미만

                // when & then
                assertThrows(PasswordException.InvalidPasswordFormatException.class, () -> {
                    User.of(lastName, firstName, email, shortPassword, Role.USER);
                });
            }

            @Test
            @DisplayName("비밀번호는 대문자를 포함해야 한다")
            void passwordMustContainUppercase() {
                // given
                String lastName = "김";
                String firstName = "철수";
                String email = "test@example.com";
                String noUppercasePassword = "password1!"; // 대문자 없음

                // when & then
                assertThrows(PasswordException.InvalidPasswordFormatException.class, () -> {
                    User.of(lastName, firstName, email, noUppercasePassword, Role.USER);
                });
            }

            @Test
            @DisplayName("비밀번호는 숫자를 포함해야 한다")
            void passwordMustContainDigit() {
                // given
                String lastName = "김";
                String firstName = "철수";
                String email = "test@example.com";
                String noDigitPassword = "Password!"; // 숫자 없음

                // when & then
                assertThrows(PasswordException.InvalidPasswordFormatException.class, () -> {
                    User.of(lastName, firstName, email, noDigitPassword, Role.USER);
                });
            }

            @Test
            @DisplayName("비밀번호는 특수문자를 포함해야 한다")
            void passwordMustContainSpecialCharacter() {
                // given
                String lastName = "김";
                String firstName = "철수";
                String email = "test@example.com";
                String noSpecialCharPassword = "Password1"; // 특수문자 없음

                // when & then
                assertThrows(PasswordException.InvalidPasswordFormatException.class, () -> {
                    User.of(lastName, firstName, email, noSpecialCharPassword, Role.USER);
                });
            }
        }
    }

    @Nested
    @DisplayName("값 객체 동등성 테스트")
    class ValueObjectEqualityTest {

        @Test
        @DisplayName("같은 값을 가진 이메일 객체는 동등하다")
        void emailsWithSameValueAreEqual() {
            // given
            String emailValue = "test@example.com";
            Email email1 = Email.of(emailValue);
            Email email2 = Email.of(emailValue);

            // when & then
            assertEquals(email1, email2);
            assertEquals(email1.hashCode(), email2.hashCode());
        }

        @Test
        @DisplayName("같은 값을 가진 비밀번호 객체는 동등하다")
        void passwordsWithSameValueAreEqual() {
            // given
            String passwordValue = "Password1!";
            Password password1 = Password.of(passwordValue);
            Password password2 = Password.of(passwordValue);

            // when & then
            assertEquals(password1, password2);
            assertEquals(password1.hashCode(), password2.hashCode());
        }

        @Test
        @DisplayName("같은 값을 가진 전체 이름 객체는 동등하다")
        void fullNamesWithSameValueAreEqual() {
            // given
            String lastName = "김";
            String firstName = "철수";
            FullName fullName1 = FullName.of(lastName, firstName);
            FullName fullName2 = FullName.of(lastName, firstName);

            // when & then
            assertEquals(fullName1, fullName2);
            assertEquals(fullName1.hashCode(), fullName2.hashCode());
        }
    }
        }

