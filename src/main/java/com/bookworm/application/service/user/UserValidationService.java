package com.bookworm.application.service.user;

/**
 * 사용자 정보 검증 관련 서비스 인터페이스
 */
public interface UserValidationService extends UserService {

    /**
     * 이메일 존재 여부 확인
     *
     * @param email 확인할 이메일
     * @return 존재 여부 (true: 존재함, false: 존재하지 않음)
     */
    boolean existsByEmail(String email);

}