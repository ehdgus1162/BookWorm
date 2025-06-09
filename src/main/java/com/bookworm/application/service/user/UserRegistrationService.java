package com.bookworm.application.service.user;

import com.bookworm.application.dto.SignUpRequest;
import com.bookworm.application.dto.SignUpResponse;

/**
 * 사용자 등록 관련 서비스 인터페이스
 */
public interface UserRegistrationService extends UserService {

    /**
     * 새 사용자 등록(회원가입) 처리
     *
     * @param request 사용자 등록 요청 정보
     * @return 등록된 사용자 정보
     */
    SignUpResponse signUp(SignUpRequest request);
}