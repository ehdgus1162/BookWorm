package com.bookworm.infrastructure.config;

import com.bookworm.domain.entity.User;
import com.bookworm.domain.repository.UserRepository;
import com.bookworm.domain.vo.user.Email;
import com.bookworm.domain.vo.user.Address;
import com.bookworm.domain.vo.user.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.bookworm.domain.vo.user.FirstName;
import com.bookworm.domain.vo.user.LastName;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultAdminCreator implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 기본 관리자 계정 정보
    private static final String DEFAULT_ADMIN_EMAIL = "admin@bookworm.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin123!";
    private static final String DEFAULT_ADMIN_FIRST_NAME = "System";
    private static final String DEFAULT_ADMIN_LAST_NAME = "Admin";
    private static final String DEFAULT_ADMIN_PHONE = "010-0000-0000";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createDefaultAdminIfNotExists();
    }

    private void createDefaultAdminIfNotExists() {
        try {
            Email adminEmail = Email.of(DEFAULT_ADMIN_EMAIL);

            boolean adminExists = userRepository.existsByEmail(adminEmail);

            if (!adminExists) {
                // 기본 주소 정보 생성 - 생성자 사용
                Address defaultAddress = new Address(
                        "시스템 관리자 주소",    // street
                        "서울시",              // city
                        "서울특별시",          // state
                        "대한민국"            // country
                );

                // 기본 전화번호 생성 - 생성자 사용
                PhoneNumber defaultPhone = new PhoneNumber(DEFAULT_ADMIN_PHONE);

                // 기본 관리자 계정 생성
                User defaultAdmin = User.createAdmin(
                        adminEmail,
                        DEFAULT_ADMIN_PASSWORD,
                        new FirstName(DEFAULT_ADMIN_FIRST_NAME),
                        new LastName(DEFAULT_ADMIN_LAST_NAME),
                        defaultAddress,   // ← new Address()로 생성
                        defaultPhone,     // ← new PhoneNumber()로 생성
                        passwordEncoder
                );

                userRepository.save(defaultAdmin);

                log.info("=".repeat(60));
                log.info("기본 관리자 계정이 생성되었습니다!");
                log.info("이메일: {}", DEFAULT_ADMIN_EMAIL);
                log.info("비밀번호: {}", DEFAULT_ADMIN_PASSWORD);
                log.info("이름: {} {}", DEFAULT_ADMIN_FIRST_NAME, DEFAULT_ADMIN_LAST_NAME);
                log.info("전화번호: {}", DEFAULT_ADMIN_PHONE);
                log.info("=".repeat(60));
                log.warn("⚠️  보안을 위해 운영 환경에서는 반드시 비밀번호를 변경하세요!");
                log.info("=".repeat(60));
            } else {
                log.debug("기본 관리자 계정이 이미 존재합니다: {}", DEFAULT_ADMIN_EMAIL);
            }

        } catch (Exception e) {
            log.error("기본 관리자 계정 생성 중 오류가 발생했습니다", e);
        }
    }
}