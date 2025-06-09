package com.bookworm.interfaces.controller;

import com.bookworm.application.dto.SignUpRequest;
import com.bookworm.application.dto.SignUpResponse;
import com.bookworm.application.service.user.UserSignUpService;
import com.bookworm.interfaces.common.ApiResponse;
import com.bookworm.interfaces.common.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 관리 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserSignUpService userSignUpService;
    private final ResponseHelper responseHelper;

    /**
     * 모든 사용자 목록 조회 API (기존 방식)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SignUpResponse>>> getAllUsers() {
        log.info("모든 사용자 목록 조회 요청");

        List<SignUpResponse> users = userSignUpService.findAllUsers();

        return ResponseEntity.ok(
                ApiResponse.success(users, "사용자 목록을 성공적으로 조회했습니다.")
        );
    }

    /**
     * 페이징 적용 사용자 목록 조회 API (새로 추가)
     */
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Page<SignUpResponse>>> getAllUsersPaged(
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("페이징 적용 사용자 목록 조회 요청: 페이지={}, 크기={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<SignUpResponse> users = userSignUpService.findAllUsers(pageable);

        return ResponseEntity.ok(
                ApiResponse.success(users, "사용자 목록을 성공적으로 조회했습니다.")
        );
    }

    /**
     * 특정 사용자 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SignUpResponse>> getUserById(@PathVariable Long id) {
        log.info("사용자 조회 요청: ID={}", id);

        SignUpResponse user = userSignUpService.findUserById(id);

        return ResponseEntity.ok(
                ApiResponse.success(user, "사용자 정보를 성공적으로 조회했습니다.")
        );
    }

    /**
     * 사용자 정보 업데이트 API
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SignUpResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody SignUpRequest request) {

        log.info("사용자 정보 업데이트 요청: ID={}, 이메일={}", id, request.email());

        SignUpResponse updatedUser = userSignUpService.updateUser(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(updatedUser, "사용자 정보가 성공적으로 업데이트되었습니다.")
        );
    }

    /**
     * 사용자 삭제 API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("사용자 삭제 요청: ID={}", id);

        userSignUpService.deleteUser(id);

        return responseHelper.noContent("사용자가 성공적으로 삭제되었습니다.");
    }
}