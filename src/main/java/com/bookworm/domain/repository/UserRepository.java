package com.bookworm.domain.repository;

import com.bookworm.domain.entity.User;
import com.bookworm.domain.vo.user.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * User Repository 인터페이스
 *
 * 추가된 메서드:
 * - List<User> findAll() : 전체 조회 (페이징 없음)
 * - Page<User> findAll(Pageable) : 페이징 조회
 */
public interface UserRepository {

    // =================================
    // 조회 메서드들
    // =================================

    /**
     * 전체 User 조회 (페이징 없음)
     * 모든 사용자를 List로 반환합니다.
     */
    List<User> findAll();

    /**
     * 페이징된 User 조회
     * Pageable 파라미터에 따라 페이징 처리된 결과를 반환합니다.
     */
    Page<User> findAll(Pageable pageable);

    /**
     * ID로 User 조회
     */
    Optional<User> findById(Long id);

    /**
     * 이메일로 User 조회
     */
    Optional<User> findByEmail(Email email);

    /**
     * 활성 User 목록 조회
     */
    List<User> findActiveUsers();

    // =================================
    // 존재 여부 확인 메서드들
    // =================================

    /**
     * ID로 User 존재 여부 확인
     */
    boolean existsById(Long id);

    /**
     * 이메일로 User 존재 여부 확인
     */
    boolean existsByEmail(Email email);

    // =================================
    // 저장/수정/삭제 메서드들
    // =================================

    /**
     * User 저장 또는 수정
     */
    User save(User user);

    /**
     * User 삭제
     */
    void delete(User user);

    /**
     * ID로 User 삭제
     */
    void deleteById(Long id);
}