package com.bookworm.application.service.user;

import com.bookworm.domain.repository.UserRepository;
import com.bookworm.domain.vo.user.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 정보 검증 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserValidationServiceImpl implements UserValidationService {

    private final UserRepository userRepository;

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(Email.of(email)).isPresent();
    }
}