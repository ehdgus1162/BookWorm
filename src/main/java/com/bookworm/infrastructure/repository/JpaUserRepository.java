package com.bookworm.infrastructure.repository;

import com.bookworm.domain.entity.User;
import com.bookworm.domain.vo.user.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User JPA Repository 인터페이스
 */
public interface JpaUserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(Email email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(Email email);

    /**
     * 활성 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' ORDER BY u.createdAt DESC")
    List<User> findActiveUsers();

    /**
     * 최근 로그인한 사용자들 조회
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginDate > :since ORDER BY u.lastLoginDate DESC")
    List<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since);

    /**
     * 휴면 계정 조회
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginDate < :cutoffDate OR u.lastLoginDate IS NULL")
    Page<User> findDormantUsers(@Param("cutoffDate") LocalDateTime cutoffDate, Pageable pageable);

}