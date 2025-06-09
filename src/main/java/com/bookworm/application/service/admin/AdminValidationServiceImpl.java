package com.bookworm.application.service.admin;

import com.bookworm.domain.repository.AdminRepository;
import com.bookworm.domain.vo.user.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 검증 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminValidationServiceImpl implements AdminValidationService {

    private final AdminRepository adminRepository;

    @Override
    public boolean existsByEmail(String email) {
        return adminRepository.existsAdminByEmail(Email.of(email));
    }
}