package com.bookworm.domain.user.repository;

import com.bookworm.domain.user.entity.User;
import com.bookworm.domain.vo.value.Email;

import java.util.Optional;

/**
 * 사용자 엔티티에 대한 저장소 인터페이스
 * 도메인 계층에 정의하여 영속성 기술로부터 도메인 모델을 독립시킴
 */
public interface UserRepository {

    /**
     * 사용자 저장
     * @param user 저장할 사용자 엔티티
     * @return 저장된 사용자 엔티티
     */
    User save(User user);
    /**
     * ID로 사용자 조회
     * @param id 조회할 사용자 ID
     * @return 조회된 사용자 (없으면 빈 Optional)
     */
    Optional<User> findById(Long id);

    /**
     * 이메일로 사용자 조회
     * @param email 조회할 사용자 이메일
     * @return 조회된 사용자 (없으면 빈 Optional)
     */
    Optional<User> findByEmail(Email email);

    /**
     * 모든 사용자 조회
     * @return 모든 사용자 목록
     */
    Iterable<User> findAll();

    /**
     * 사용자 삭제
     * @param user 삭제할 사용자 엔티티
     */
    void delete(User user);
}

