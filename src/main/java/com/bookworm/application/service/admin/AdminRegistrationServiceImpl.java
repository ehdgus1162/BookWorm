package com.bookworm.application.service.admin;

import com.bookworm.application.dto.AdminSignUpRequest;
import com.bookworm.application.dto.AdminSignUpResponse;
import com.bookworm.application.service.common.EmailService;
import com.bookworm.domain.entity.User;
import com.bookworm.domain.vo.user.*;
import com.bookworm.domain.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 등록 서비스 구현체
 * User 엔티티의 createAdmin 팩토리 메서드 활용
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminRegistrationServiceImpl implements AdminRegistrationService {

    private final AdminRepository adminRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AdminSignUpResponse signUp(AdminSignUpRequest req) {
        // 1. 관리자 이메일 중복 체크 (일반 사용자와 분리)
        Email email = Email.of(req.email());
        if (adminRepository.existsAdminByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 관리자 이메일입니다.");
        }

        // 2. Address 및 PhoneNumber 생성 (AdminSignUpRequest에서 가져오거나 기본값 설정)
        Address address;
        PhoneNumber phoneNumber;

        try {
            // AdminSignUpRequest에 주소 정보가 있다면 사용
            if (hasAddressInfo(req)) {
                address = new Address(
                        req.street() != null ? req.street() : "관리자 주소",
                        req.city() != null ? req.city() : "서울시",
                        req.state() != null ? req.state() : "서울특별시",
                        req.country() != null ? req.country() : "대한민국"
                );
            } else {
                // 기본 주소 설정
                address = new Address(
                        "관리자 주소",     // street
                        "서울시",         // city
                        "서울특별시",     // state
                        "대한민국"       // country
                );
            }

            // AdminSignUpRequest에 전화번호가 있다면 사용, 없으면 기본값
            if (req.phoneNumber() != null && !req.phoneNumber().isBlank()) {
                phoneNumber = new PhoneNumber(req.phoneNumber());
            } else {
                phoneNumber = new PhoneNumber("010-0000-0000"); // 기본값
            }

        } catch (Exception e) {
            log.warn("Address/PhoneNumber 생성 중 오류, 기본값 사용: {}", e.getMessage());
            // 오류 시 안전한 기본값 사용
            address = new Address("N/A", "Seoul", "Seoul", "Korea");
            phoneNumber = new PhoneNumber("010-1234-5678");
        }

        // 3. User 엔티티의 createAdmin 팩토리 메서드 활용 (수정된 시그니처)
        User admin = User.createAdmin(
                email,
                req.password(),
                new FirstName(req.firstName()),
                new LastName(req.lastName()),
                address,      // ← Address 추가
                phoneNumber,  // ← PhoneNumber 추가
                passwordEncoder
        );

        // 4. 저장
        adminRepository.save(admin);
        log.info("새 관리자가 등록되었습니다. ID: {}, 이메일: {}", admin.getId(), admin.getEmail().getValue());

        // 5. 응답 생성
        return AdminSignUpResponse.from(admin);
    }

    /**
     * AdminSignUpRequest에 주소 정보가 포함되어 있는지 확인
     */
    private boolean hasAddressInfo(AdminSignUpRequest req) {
        return (req.street() != null && !req.street().isBlank()) ||
                (req.city() != null && !req.city().isBlank()) ||
                (req.state() != null && !req.state().isBlank()) ||
                (req.country() != null && !req.country().isBlank());
    }
}