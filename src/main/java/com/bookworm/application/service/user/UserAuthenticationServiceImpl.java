package com.bookworm.application.service.user;

import com.bookworm.application.dto.LoginResponse;
import com.bookworm.application.service.common.PasswordService;
import com.bookworm.domain.constant.UserStatus;
import com.bookworm.domain.entity.User;
import com.bookworm.domain.exception.AuthenticationException;
import com.bookworm.domain.repository.UserRepository;
import com.bookworm.domain.vo.user.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 인증 서비스 구현체
 * 기존 서비스들(PasswordService, UserQueryService)을 활용하여 인증 로직 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAuthenticationServiceImpl implements UserAuthenticationService {

    private final UserRepository userRepository;
    private final UserQueryService userQueryService;
    private final PasswordService passwordService;

    @Override
    public LoginResponse authenticate(String email, String password) {
        log.info("사용자 인증 시도: {}", email);

        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(Email.of(email))
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 이메일로 로그인 시도: {}", email);
                    return new AuthenticationException("이메일 또는 비밀번호가 올바르지 않습니다.");
                });

        // 2. 계정 상태 확인
        if (!isActiveUser(user)) {
            log.warn("비활성 계정 로그인 시도: 사용자 ID={}, 상태={}", user.getId(), user.getStatus());
            throw new AuthenticationException("계정이 비활성화되어 있습니다. 관리자에게 문의하세요.");
        }

        // 3. 비밀번호 검증 (기존 PasswordService 활용)
        if (!passwordService.matches(password, user.getPassword())) {
            log.warn("비밀번호 불일치: 사용자 ID={}", user.getId());
            throw new AuthenticationException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        log.info("사용자 인증 성공: 사용자 ID={}, 이름={}", user.getId(), user.getFirstName().getValue());
        return LoginResponse.from(user);
    }

    @Override
    public LoginResponse findUserForSession(Long userId) {
        log.debug("세션 검증을 위한 사용자 조회: 사용자 ID={}", userId);

        // 기존 UserQueryService 활용
        User user = userQueryService.getUserEntityById(userId);

        // 계정 상태 재확인 (세션 중간에 계정이 비활성화될 수 있음)
        if (!isActiveUser(user)) {
            log.warn("세션 검증 중 비활성 계정 발견: 사용자 ID={}, 상태={}", userId, user.getStatus());
            throw new AuthenticationException("계정이 비활성화되어 있습니다.");
        }

        return LoginResponse.from(user);
    }

    /**
     * 사용자 계정이 활성 상태인지 확인
     *
     * @param user 확인할 사용자
     * @return 활성 상태 여부
     */
    private boolean isActiveUser(User user) {
        // UserStatus enum에 따라 구현
        return user.getStatus() == UserStatus.ACTIVE;
    }
}