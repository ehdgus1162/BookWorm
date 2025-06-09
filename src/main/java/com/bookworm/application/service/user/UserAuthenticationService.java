package com.bookworm.application.service.user;

import com.bookworm.application.dto.LoginResponse;
import com.bookworm.domain.exception.AuthenticationException;

/**
 * 사용자 인증 관련 서비스 인터페이스
 * 로그인, 인증 검증 등의 기능을 담당
 */
public interface UserAuthenticationService extends UserService {

    /**
     * 사용자 인증 (로그인)
     * 이메일과 비밀번호를 검증하여 사용자 정보 반환
     *
     * @param email 사용자 이메일
     * @param password 평문 비밀번호
     * @return 인증된 사용자 정보
     * @throws AuthenticationException 인증 실패 시
     */
    LoginResponse authenticate(String email, String password);

    /**
     * 사용자 ID로 기본 정보 조회 (세션 검증용)
     * 계정 상태도 함께 확인
     *
     * @param userId 사용자 ID
     * @return 사용자 기본 정보
     * @throws AuthenticationException 계정이 비활성화된 경우
     */
    LoginResponse findUserForSession(Long userId);
}