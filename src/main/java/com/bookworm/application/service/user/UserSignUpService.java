package com.bookworm.application.service.user;

import com.bookworm.application.dto.LoginResponse;
import com.bookworm.application.dto.SignUpRequest;
import com.bookworm.application.dto.SignUpResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 사용자 관련 통합 서비스 인터페이스
 */
public interface UserSignUpService {

    // 등록 관련
    SignUpResponse signUp(SignUpRequest request);

    // 조회 관련 (기존)
    List<SignUpResponse> findAllUsers();

    // 조회 관련 (페이징 추가)
    Page<SignUpResponse> findAllUsers(Pageable pageable);

    SignUpResponse findUserById(Long id);

    // 관리 관련
    SignUpResponse updateUser(Long id, SignUpRequest request);
    void deleteUser(Long id);

    // 검증 관련
    boolean existsByEmail(String email);

    // 인증 관련
    LoginResponse authenticate(String email, String password);
    LoginResponse findUserForSession(Long userId);
}