package com.bookworm.domain.user.service;

import com.bookworm.domain.user.constant.Role;
import com.bookworm.domain.user.entity.User;
import com.bookworm.domain.user.repository.UserRepository;
import com.bookworm.domain.vo.exception.EmailException;
import com.bookworm.domain.vo.exception.NameException;
import com.bookworm.domain.vo.exception.PasswordException;
import com.bookworm.domain.vo.value.Password;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDomainServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserDomainService userDomainService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private String validLastName;
    private String validFirstName;
    private String validEmail;
    private String validPassword;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        validLastName = "김";
        validFirstName = "철수";
        validEmail = "test@example.com";
        validPassword = "Password1!";
        encodedPassword = "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLM";

        // 비밀번호 인코더 설정
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
    }

    @Nested
    @DisplayName("사용자 등록 테스트")
    class RegisterUserTest {

        @Test
        @DisplayName("유효한 정보로 사용자 등록 성공")
        void registerUserSuccess() {
            // given
            User expectedUser = mock(User.class);
            when(userRepository.save(any(User.class))).thenReturn(expectedUser);

            // when
            User result = userDomainService.registerUser(validLastName, validFirstName, validEmail, validPassword);

            // then
            assertEquals(expectedUser, result);
            verify(userRepository).save(userCaptor.capture());
            User capturedUser = userCaptor.getValue();

            // 저장된 사용자의 값 검증
            assertEquals(validEmail, capturedUser.getEmail().getValue());
            assertEquals(validLastName + validFirstName, capturedUser.getName().getFullName());
            assertTrue(capturedUser.getPassword().isEncrypted());
            assertEquals(encodedPassword, capturedUser.getPassword().getValue());
            assertEquals(Role.USER, capturedUser.getRole());
        }

        @Test
        @DisplayName("관리자 등록 성공")
        void registerAdminSuccess() {
            // given
            User expectedUser = mock(User.class);
            when(userRepository.save(any(User.class))).thenReturn(expectedUser);

            // when
            User result = userDomainService.registerAdmin(validLastName, validFirstName, validEmail, validPassword);

            // then
            assertEquals(expectedUser, result);
            verify(userRepository).save(userCaptor.capture());
            assertEquals(Role.ADMIN, userCaptor.getValue().getRole());
        }

        @Test
        @DisplayName("유효하지 않은 이메일로 사용자 등록 실패")
        void registerUserWithInvalidEmailFails() {
            // given
            String invalidEmail = "invalid-email";

            // when & then
            assertThrows(EmailException.InvalidEmailFormatException.class, () -> {
                userDomainService.registerUser(validLastName, validFirstName, invalidEmail, validPassword);
            });

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("유효하지 않은 비밀번호로 사용자 등록 실패")
        void registerUserWithInvalidPasswordFails() {
            // given
            String weakPassword = "weak";

            // when & then
            assertThrows(PasswordException.InvalidPasswordFormatException.class, () -> {
                userDomainService.registerUser(validLastName, validFirstName, validEmail, weakPassword);
            });

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("유효하지 않은 이름으로 사용자 등록 실패")
        void registerUserWithInvalidNameFails() {
            // given
            String tooShortFirstName = "김"; // 2글자 미만

            // when & then
            assertThrows(NameException.InvalidNameLengthException.class, () -> {
                userDomainService.registerUser(validLastName, tooShortFirstName, validEmail, validPassword);
            });

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("비밀번호 변경 테스트")
    class ChangePasswordTest {

        private User existingUser;
        private Long userId;
        private String currentPassword;
        private String newPassword;

        @BeforeEach
        void setUp() {
            userId = 1L;
            currentPassword = validPassword;
            newPassword = "NewPassword1!";

            // 기존 사용자 모의 객체 생성
            existingUser = mock(User.class);
            Password mockedPassword = mock(Password.class);

            when(existingUser.getId()).thenReturn(userId);
            when(existingUser.getPassword()).thenReturn(mockedPassword);
            when(mockedPassword.getValue()).thenReturn(encodedPassword);

            // 비밀번호 매칭 성공 설정
            when(passwordEncoder.matches(currentPassword, encodedPassword)).thenReturn(true);

            // 비밀번호 변경 후 새 사용자 반환 설정
            User updatedUser = mock(User.class);
            when(existingUser.withEncryptedPassword(anyString())).thenReturn(updatedUser);
            when(userRepository.save(updatedUser)).thenReturn(updatedUser);

            // 사용자 조회 설정
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        }

        @Test
        @DisplayName("비밀번호 변경 성공")
        void changePasswordSuccess() {
            // when
            User result = userDomainService.changePassword(userId, currentPassword, newPassword);

            // then
            assertNotNull(result);
            verify(passwordEncoder).matches(currentPassword, encodedPassword);
            verify(passwordEncoder).encode(newPassword);
            verify(existingUser).withEncryptedPassword(anyString());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("현재 비밀번호가 일치하지 않으면 변경 실패")
        void changePasswordFailsWithIncorrectCurrentPassword() {
            // given
            String wrongCurrentPassword = "WrongPassword1!";
            when(passwordEncoder.matches(wrongCurrentPassword, encodedPassword)).thenReturn(false);

            // when & then
            assertThrows(IllegalArgumentException.class, () -> {
                userDomainService.changePassword(userId, wrongCurrentPassword, newPassword);
            });

            verify(existingUser, never()).withEncryptedPassword(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 비밀번호 변경 실패")
        void changePasswordFailsWithNonExistentUser() {
            // given
            Long nonExistentUserId = 999L;
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            // when & then
            assertThrows(IllegalArgumentException.class, () -> {
                userDomainService.changePassword(nonExistentUserId, currentPassword, newPassword);
            });
        }

        @Test
        @DisplayName("새 비밀번호가 유효하지 않으면 변경 실패")
        void changePasswordFailsWithInvalidNewPassword() {
            // given
            String invalidNewPassword = "weak";

            // when & then
            assertThrows(PasswordException.InvalidPasswordFormatException.class, () -> {
                userDomainService.changePassword(userId, currentPassword, invalidNewPassword);
            });

            verify(existingUser, never()).withEncryptedPassword(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("통합 테스트")
    class IntegrationTest {

        @Test
        @DisplayName("사용자 등록 후 비밀번호 변경 통합 테스트")
        void registerAndChangePassword() {
            // given
            User registeredUser = mock(User.class);
            Password mockedPassword = mock(Password.class);
            when(registeredUser.getId()).thenReturn(1L);
            when(registeredUser.getPassword()).thenReturn(mockedPassword);
            when(mockedPassword.getValue()).thenReturn(encodedPassword);

            User updatedUser = mock(User.class);
            when(registeredUser.withEncryptedPassword(anyString())).thenReturn(updatedUser);

            // 등록 시 저장 설정
            when(userRepository.save(any(User.class))).thenReturn(registeredUser);

            // 비밀번호 변경 시 조회 설정
            when(userRepository.findById(1L)).thenReturn(Optional.of(registeredUser));
            when(userRepository.save(updatedUser)).thenReturn(updatedUser);

            // 비밀번호 매칭 설정
            when(passwordEncoder.matches(validPassword, encodedPassword)).thenReturn(true);

            // when - 사용자 등록
            User user = userDomainService.registerUser(validLastName, validFirstName, validEmail, validPassword);

            // then - 등록 확인
            assertNotNull(user);
            assertEquals(registeredUser, user);

            // when - 비밀번호 변경
            String newPassword = "NewPassword1!";
            User changedUser = userDomainService.changePassword(user.getId(), validPassword, newPassword);

            // then - 변경 확인
            assertNotNull(changedUser);
            assertEquals(updatedUser, changedUser);

            // 순서 검증
            verify(userRepository, times(2)).save(any(User.class));
        }
    }
}