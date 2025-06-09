package com.bookworm.application.service.user;

import com.bookworm.application.dto.SignUpRequest;
import com.bookworm.application.dto.SignUpResponse;
import com.bookworm.application.service.common.EmailService;
import com.bookworm.application.service.common.PasswordService;
import com.bookworm.application.service.common.UserCreationService;
import com.bookworm.domain.entity.User;
import com.bookworm.domain.repository.UserRepository;
import com.bookworm.domain.vo.user.Email;
import com.bookworm.domain.vo.user.Password;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 등록 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final UserCreationService userCreationService;
    private final EmailService emailService;

    @Override
    public SignUpResponse signUp(SignUpRequest req) {
        // 이메일 중복 체크
        Email email = Email.of(req.email());
        emailService.checkDoesNotExist(email);

        // 비밀번호 암호화
        Password password = passwordService.encrypt(req.password());

        // 사용자 생성 (UserCreationService에 위임)
        User user = userCreationService.createUser(req, password);

        // 저장
        userRepository.save(user);
        log.info("새 사용자가 등록되었습니다. ID: {}, 이메일: {}", user.getId(), user.getEmail().getValue());

        // 응답 생성
        return SignUpResponse.from(user);
    }
}