package com.bookworm.application.service.user;

import com.bookworm.application.dto.SignUpResponse;
import com.bookworm.domain.entity.User;
import com.bookworm.domain.exception.UserNotFoundException;
import com.bookworm.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 사용자 조회 서비스 구현체
 *
 * 핵심 수정사항:
 * - Pageable.unpaged() 사용 금지
 * - 직접 List<User> findAll() 호출
 * - 안전한 페이징 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    /**
     * 모든 사용자 조회 (페이징 없음)
     *
     * 핵심 수정: UserRepository.findAll() 직접 호출
     * - Pageable.unpaged() 사용하지 않음
     * - UnsupportedOperationException 발생하지 않음
     */
    @Override
    public List<SignUpResponse> findAllUsers() {
        log.debug("전체 사용자 조회 요청");

        try {
            // ✅ 수정된 부분: 직접 List<User> 반환 메서드 호출
            List<User> users = userRepository.findAll(); // Page가 아닌 List 직접 반환

            List<SignUpResponse> responses = users.stream()
                    .map(SignUpResponse::from)
                    .collect(Collectors.toList());

            log.debug("전체 사용자 조회 완료: 총 {}명", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("전체 사용자 조회 실패", e);

            // 폴백: 활성 사용자라도 조회
            return findActiveUsersAsFallback();
        }
    }

    /**
     * 페이징된 사용자 조회
     *
     * 안전한 페이징 처리 구현
     */
    @Override
    public Page<SignUpResponse> findAllUsers(Pageable pageable) {
        log.debug("페이징 사용자 조회 요청: {}", pageable);

        try {
            // Repository의 안전한 페이징 메서드 호출
            Page<User> userPage = userRepository.findAll(pageable);

            // User -> SignUpResponse 변환
            Page<SignUpResponse> responsePage = userPage.map(SignUpResponse::from);

            log.debug("페이징 사용자 조회 완료: Page={}, Size={}, Total={}",
                    userPage.getNumber(),
                    userPage.getSize(),
                    responsePage.getTotalElements());

            return responsePage;

        } catch (Exception e) {
            log.error("페이징 사용자 조회 실패", e);
            throw new RuntimeException("페이징 사용자 조회에 실패했습니다.", e);
        }
    }

    /**
     * 폴백 메서드: 활성 사용자 조회
     */
    private List<SignUpResponse> findActiveUsersAsFallback() {
        try {
            log.warn("전체 사용자 조회 실패, 활성 사용자로 대체합니다.");

            List<User> activeUsers = userRepository.findActiveUsers();

            List<SignUpResponse> responses = activeUsers.stream()
                    .map(SignUpResponse::from)
                    .collect(Collectors.toList());

            log.warn("활성 사용자로 대체 완료: {}명", responses.size());
            return responses;

        } catch (Exception fallbackException) {
            log.error("활성 사용자 조회도 실패", fallbackException);
            throw new RuntimeException("사용자 목록 조회에 완전히 실패했습니다.", fallbackException);
        }
    }

    /**
     * ID로 사용자 조회 (DTO 반환)
     */
    @Override
    public SignUpResponse findUserById(Long id) {
        log.debug("사용자 조회 요청: ID={}", id);

        User user = getUserEntityById(id);
        SignUpResponse response = SignUpResponse.from(user);

        log.debug("사용자 조회 완료: ID={}, Email={}",
                user.getId(),
                user.getEmail() != null ? user.getEmail().getValue() : "null");

        return response;
    }

    /**
     * ID로 사용자 엔티티 조회
     */
    @Override
    public User getUserEntityById(Long id) {
        // 1. 입력값 검증
        Objects.requireNonNull(id, "사용자 ID는 필수입니다");

        if (id <= 0) {
            throw new IllegalArgumentException("사용자 ID는 양수여야 합니다: " + id);
        }

        log.debug("사용자 엔티티 조회: ID={}", id);

        try {
            // 2. Repository에서 조회
            return userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("사용자를 찾을 수 없음: ID={}", id);
                        return new UserNotFoundException("ID에 해당하는 사용자를 찾을 수 없습니다: " + id);
                    });

        } catch (UserNotFoundException e) {
            // UserNotFoundException은 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("사용자 조회 중 예상치 못한 오류 발생: ID={}", id, e);
            throw new RuntimeException("사용자 조회에 실패했습니다: " + id, e);
        }
    }

    // =================================
    // 추가 편의 메서드들
    // =================================

    /**
     * 안전한 페이징 조회 (예외 발생 시 기본 페이징으로 폴백)
     */
    public Page<SignUpResponse> findAllUsersSafely(Pageable pageable) {
        try {
            return findAllUsers(pageable);
        } catch (Exception e) {
            log.warn("페이징 조회 실패, 기본 페이징으로 폴백합니다", e);

            // 안전한 기본 페이징으로 재시도
            Pageable safePage = PageRequest.of(0, 20);
            Page<User> userPage = userRepository.findAll(safePage);
            return userPage.map(SignUpResponse::from);
        }
    }

    /**
     * 활성 사용자만 조회
     */
    public List<SignUpResponse> findActiveUsers() {
        log.debug("활성 사용자 조회 요청");

        try {
            List<User> activeUsers = userRepository.findActiveUsers();

            List<SignUpResponse> responses = activeUsers.stream()
                    .map(SignUpResponse::from)
                    .collect(Collectors.toList());

            log.debug("활성 사용자 조회 완료: 총 {}명", responses.size());
            return responses;

        } catch (Exception e) {
            log.error("활성 사용자 조회 실패", e);
            throw new RuntimeException("활성 사용자 조회에 실패했습니다.", e);
        }
    }

    /**
     * 사용자 존재 여부 확인
     */
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, "사용자 ID는 필수입니다");

        try {
            boolean exists = userRepository.existsById(id);
            log.debug("사용자 존재 여부 확인: ID={}, exists={}", id, exists);
            return exists;
        } catch (Exception e) {
            log.error("사용자 존재 여부 확인 실패: ID={}", id, e);
            throw new RuntimeException("사용자 존재 여부 확인에 실패했습니다.", e);
        }
    }
}