package com.bookworm.domain.repository;

import com.bookworm.domain.entity.User;
import com.bookworm.domain.vo.user.Email;

import java.util.List;
import java.util.Optional;

/**
 * 관리자 도메인 Repository 인터페이스
 *
 * 역할:
 * - 도메인 로직에서 필요한 관리자 관련 데이터 접근 계약 정의
 * - 구현 기술(JPA)에 의존하지 않는 순수한 도메인 인터페이스
 * - 비즈니스 의도가 명확한 메서드명 사용
 */
public interface AdminRepository {

    /**
     * 관리자 저장
     */
    User save(User admin);

    /**
     * 관리자 이메일로 조회
     */
    Optional<User> findAdminByEmail(Email email);

    /**
     * 모든 관리자 조회 (최신순)
     */
    List<User> findAllAdmins();

    /**
     * 활성 관리자만 조회
     */
    List<User> findActiveAdmins();

    /**
     * 관리자 이메일 중복 확인
     */
    boolean existsAdminByEmail(Email email);

    /**
     * 관리자 ID로 조회 (관리자 역할 확인 포함)
     */
    Optional<User> findAdminById(Long id);

    /**
     * 관리자 삭제
     */
    void delete(User admin);

}