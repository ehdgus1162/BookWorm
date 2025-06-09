package com.bookworm.application.service.admin;

import com.bookworm.application.dto.AdminSignUpResponse;
import com.bookworm.application.service.admin.AdminQueryService;
import com.bookworm.domain.entity.User;
import com.bookworm.domain.exception.UserNotFoundException;
import com.bookworm.domain.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 관리자 조회 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminQueryServiceImpl implements AdminQueryService {

    private final AdminRepository adminRepository;

    @Override
    public List<AdminSignUpResponse> findAllAdmins() {
        List<User> admins = adminRepository.findAllAdmins();

        return admins.stream()
                .map(AdminSignUpResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public AdminSignUpResponse findAdminById(Long id) {
        User admin = getAdminEntityById(id);
        return AdminSignUpResponse.from(admin);
    }

    @Override
    public User getAdminEntityById(Long id) {
        Objects.requireNonNull(id, "관리자 ID는 필수입니다");

        User user = adminRepository.findAdminById(id)
                .orElseThrow(() -> new UserNotFoundException("ID에 해당하는 사용자를 찾을 수 없습니다: " + id));

        // 관리자 권한 확인
        if (!user.isAdmin()) {
            throw new UserNotFoundException("ID에 해당하는 관리자를 찾을 수 없습니다: " + id);
        }

        return user;
    }
}