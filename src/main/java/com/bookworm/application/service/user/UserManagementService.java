package com.bookworm.application.service.user;
import com.bookworm.application.dto.SignUpRequest;
import com.bookworm.application.dto.SignUpResponse;

/**
 * 사용자 관리 관련 서비스 인터페이스
 */
public interface UserManagementService extends UserService {

    /**
     * 사용자 정보 업데이트
     *
     * @param id 사용자 ID
     * @param request 업데이트 요청 정보
     * @return 업데이트된 사용자 정보
     */
    SignUpResponse updateUser(Long id, SignUpRequest request);

    /**
     * 사용자 삭제 (비활성화)
     *
     * @param id 사용자 ID
     */
    void deleteUser(Long id);
}