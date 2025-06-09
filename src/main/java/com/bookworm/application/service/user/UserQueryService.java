package com.bookworm.application.service.user;

import com.bookworm.application.dto.SignUpResponse;
import com.bookworm.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 사용자 조회 관련 서비스 인터페이스
 */
public interface UserQueryService extends UserService {

    /**
     * 모든 사용자 목록 조회
     *
     * @return 사용자 목록
     */
    List<SignUpResponse> findAllUsers();

    Page<SignUpResponse> findAllUsers(Pageable pageable);

    /**
     * ID로 특정 사용자 조회
     *
     * @param id 사용자 ID
     * @return 사용자 정보
     */
    SignUpResponse findUserById(Long id);

    /**
     * ID로 사용자 엔티티 조회
     * 내부 사용을 위한 메서드
     *
     * @param id 사용자 ID
     * @return 사용자 엔티티
     */
    User getUserEntityById(Long id);
}
