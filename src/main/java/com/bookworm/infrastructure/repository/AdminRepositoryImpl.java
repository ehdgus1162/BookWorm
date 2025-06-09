package com.bookworm.infrastructure.repository;

import com.bookworm.domain.entity.User;
import com.bookworm.domain.repository.AdminRepository;
import com.bookworm.domain.vo.user.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 관리자 Repository 구현체
 *
 * 역할:
 * - 도메인 Repository 인터페이스의 구현
 * - JPA Repository를 주입받아 실제 데이터 접근 수행
 * - 도메인 계층과 인프라스트럭처 계층을 연결하는 어댑터 역할
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AdminRepositoryImpl implements AdminRepository {

    private final JpaAdminRepository adminJpaRepository;

    @Override
    public User save(User admin) {
        log.debug("관리자 저장: ID={}, 이메일={}", admin.getId(), admin.getEmail().getValue());
        return adminJpaRepository.save(admin);
    }

    @Override
    public Optional<User> findAdminByEmail(Email email) {
        log.debug("관리자 이메일로 조회: {}", email.getValue());
        return adminJpaRepository.findAdminByEmail(email);
    }

    @Override
    public List<User> findAllAdmins() {
        log.debug("모든 관리자 조회");
        return adminJpaRepository.findAllAdmins();
    }

    @Override
    public List<User> findActiveAdmins() {
        log.debug("활성 관리자 조회");
        return adminJpaRepository.findActiveAdmins();
    }

    @Override
    public boolean existsAdminByEmail(Email email) {
        log.debug("관리자 이메일 중복 확인: {}", email.getValue());
        return adminJpaRepository.existsAdminByEmail(email);
    }

    @Override
    public Optional<User> findAdminById(Long id) {
        log.debug("관리자 ID로 조회: {}", id);
        return adminJpaRepository.findAdminById(id);
    }

    @Override
    public void delete(User admin) {
        log.debug("관리자 삭제: ID={}, 이메일={}", admin.getId(), admin.getEmail().getValue());
        adminJpaRepository.delete(admin);
    }
}