package com.bookworm.application.service.user;

import com.bookworm.application.dto.LoginResponse;
import com.bookworm.application.dto.SignUpRequest;
import com.bookworm.application.dto.SignUpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 관련 통합 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserSignUpServiceImpl implements UserSignUpService {

    private final UserRegistrationService userRegistrationService;
    private final UserQueryService userQueryService;
    private final UserManagementService userManagementService;
    private final UserValidationService userValidationService;
    private final UserAuthenticationService userAuthenticationService;

    @Override
    public SignUpResponse signUp(SignUpRequest request) {
        return userRegistrationService.signUp(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SignUpResponse> findAllUsers() {
        return userQueryService.findAllUsers();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SignUpResponse> findAllUsers(Pageable pageable) {
        return userQueryService.findAllUsers(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public SignUpResponse findUserById(Long id) {
        return userQueryService.findUserById(id);
    }

    @Override
    public SignUpResponse updateUser(Long id, SignUpRequest request) {
        return userManagementService.updateUser(id, request);
    }

    @Override
    public void deleteUser(Long id) {
        userManagementService.deleteUser(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userValidationService.existsByEmail(email);
    }

    // ========== 인증 관련 메서드 구현 ==========

    @Override
    @Transactional(readOnly = true)
    public LoginResponse authenticate(String email, String password) {
        log.info("통합 서비스를 통한 사용자 인증 요청: {}", email);
        return userAuthenticationService.authenticate(email, password);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse findUserForSession(Long userId) {
        log.debug("통합 서비스를 통한 세션 사용자 조회: 사용자 ID={}", userId);
        return userAuthenticationService.findUserForSession(userId);
    }
}