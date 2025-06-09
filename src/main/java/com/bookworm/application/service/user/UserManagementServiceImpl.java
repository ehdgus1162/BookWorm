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
 * 사용자 관리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final UserQueryService userQueryService;
    private final EmailService emailService;
    private final PasswordService passwordService;
    private final UserCreationService userCreationService;

    @Override
    public SignUpResponse updateUser(Long id, SignUpRequest request) {
        // 1. 기존 사용자 조회 (UserQueryService 활용)
        User existingUser = userQueryService.getUserEntityById(id);

        // 2. 이메일 변경 검증 (기존 이메일과 다른 경우만)
        Email newEmail = Email.of(request.email());
        if (!existingUser.getEmail().equals(newEmail)) {
            emailService.checkDoesNotExist(newEmail);
        }

        // 3. 비밀번호 처리
        Password password = request.password() != null && !request.password().isBlank()
                ? passwordService.encrypt(request.password())
                : existingUser.getPassword();

        // 4. 사용자 정보 업데이트 (UserCreationService에 위임)
        User updatedUser = userCreationService.updateUser(existingUser, request, password);

        // 5. 저장
//        userRepository.save(updatedUser);
        log.info("사용자 정보가 업데이트되었습니다. ID: {}", id);

        // 6. 응답 생성
        return SignUpResponse.from(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        // 1. 사용자 조회 (UserQueryService 활용)
        User existingUser = userQueryService.getUserEntityById(id);

        // 2. 사용자 비활성화 (Entity의 비즈니스 메서드 직접 사용)
        existingUser.deactivate();

        // 3. ✅ save() 호출 제거!
        // JPA Dirty Checking이 트랜잭션 종료 시 자동으로 UPDATE 쿼리 실행
        // userRepository.save() 호출하면 version 충돌 에러 발생!

        log.info("사용자가 비활성화되었습니다. ID: {}", id);
    }
}