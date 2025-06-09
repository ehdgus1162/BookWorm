package com.bookworm.infrastructure.repository;


import com.bookworm.domain.entity.User;
import com.bookworm.domain.vo.user.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 관리자 JPA Repository 인터페이스
 *
 * 역할:
 * - Spring Data JPA의 구체적인 쿼리 정의
 * - 데이터베이스 접근을 위한 기술적 인터페이스
 * - @Query 어노테이션으로 복잡한 JPQL 쿼리 정의
 */
public interface JpaAdminRepository extends JpaRepository<User, Long> {

    /**
     * 관리자 이메일로 조회
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.role = 'ADMIN'")
    Optional<User> findAdminByEmail(@Param("email") Email email);

    /**
     * 모든 관리자 조회 (최신순)
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' ORDER BY u.createdAt DESC")
    List<User> findAllAdmins();

    /**
     * 활성 관리자만 조회
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.status = 'ACTIVE'")
    List<User> findActiveAdmins();

    /**
     * 관리자 이메일 중복 확인
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u WHERE u.email = :email AND u.role = 'ADMIN'")
    boolean existsAdminByEmail(@Param("email") Email email);

    /**
     * 관리자 ID로 조회 (관리자 역할 확인 포함)
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.role = 'ADMIN'")
    Optional<User> findAdminById(@Param("id") Long id);
}