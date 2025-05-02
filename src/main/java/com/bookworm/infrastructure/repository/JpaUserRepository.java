package com.bookworm.infrastructure.repository;

import com.bookworm.domain.user.entity.User;
import com.bookworm.domain.user.repository.UserRepository;
import com.bookworm.domain.vo.value.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA를 이용한 UserRepository 구현체
 */
@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {

    /**
     * 이메일로 사용자 조회
     * JpaRepository의 쿼리 메서드 네이밍 규칙을 따름
     */
    @Override
    Optional<User> findByEmail(Email email);
}