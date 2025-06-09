package com.bookworm.application.service.admin;

import com.bookworm.application.dto.AdminLoginResponse;
import com.bookworm.application.service.common.PasswordService;
import com.bookworm.domain.common.TimeProvider;
import com.bookworm.domain.constant.UserStatus;
import com.bookworm.domain.entity.User;
import com.bookworm.domain.exception.AuthenticationException;
import com.bookworm.domain.repository.AdminRepository;
import com.bookworm.domain.vo.user.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 인증 서비스 구현체
 * User 엔티티의 ADMIN 역할만 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuthenticationServiceImpl implements AdminAuthenticationService {

    private final AdminRepository adminRepository;
    private final AdminQueryService adminQueryService;
    private final PasswordService passwordService;
    private final TimeProvider timeProvider;

    @Override
    @Transactional  // 로그인 시간 기록을 위해 쓰기 트랜잭션
    public AdminLoginResponse authenticate(String email, String password) {
        log.info("관리자 인증 시도: {}", email);

        // 1. 이메일로 관리자 조회 (ADMIN 역할만)
        User admin = adminRepository.findAdminByEmail(Email.of(email))
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 관리자 이메일로 로그인 시도: {}", email);
                    return new AuthenticationException("이메일 또는 비밀번호가 올바르지 않습니다.");
                });

        // 2. 계정 상태 확인
        if (!admin.isActive()) {
            log.warn("비활성 관리자 계정 로그인 시도: 관리자 ID={}, 상태={}", admin.getId(), admin.getStatus());
            throw new AuthenticationException("계정이 비활성화되어 있습니다. 시스템 관리자에게 문의하세요.");
        }

        // 3. 비밀번호 검증 (기존 PasswordService 활용)
        if (!passwordService.matches(password, admin.getPassword())) {
            log.warn("관리자 비밀번호 불일치: 관리자 ID={}", admin.getId());
            throw new AuthenticationException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 4. 로그인 시간 기록 (User 엔티티의 비즈니스 메서드 활용)
        admin.recordLogin(timeProvider);
        adminRepository.save(admin);

        log.info("관리자 인증 성공: 관리자 ID={}, 이름={}", admin.getId(), admin.getFullName());
        return AdminLoginResponse.from(admin);
    }

    @Override
    public AdminLoginResponse findAdminForSession(Long adminId) {
        log.debug("세션 검증을 위한 관리자 조회: 관리자 ID={}", adminId);

        // 기존 AdminQueryService 활용
        User admin = adminQueryService.getAdminEntityById(adminId);

        // 관리자 권한 재확인
        if (!admin.isAdmin()) {
            log.warn("세션 검증 중 일반 사용자 발견: 사용자 ID={}, 역할={}", adminId, admin.getRole());
            throw new AuthenticationException("관리자 권한이 없습니다.");
        }

        // 계정 상태 재확인 (세션 중간에 계정이 비활성화될 수 있음)
        if (!admin.isActive()) {
            log.warn("세션 검증 중 비활성 관리자 계정 발견: 관리자 ID={}, 상태={}", adminId, admin.getStatus());
            throw new AuthenticationException("계정이 비활성화되어 있습니다.");
        }

        return AdminLoginResponse.from(admin);
    }
}