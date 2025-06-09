package com.bookworm.infrastructure.repository;

import com.bookworm.domain.entity.User;
import com.bookworm.domain.repository.UserRepository;
import com.bookworm.domain.vo.user.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * User Repository 구현체
 *
 * Unpaged 관련 UnsupportedOperationException 해결:
 * - 전체 조회와 페이징 조회를 명확히 분리
 * - Unpaged 객체에서 getPageNumber() 호출 방지
 * - 안전한 페이징 처리 로직 구현
 */
@Repository
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaRepository;

    // =================================
    // 1. 전체 조회 메서드 (페이징 없음)
    // =================================

    /**
     * 전체 User 조회 (페이징 없음)
     * 이 메서드는 모든 데이터를 한 번에 조회합니다.
     *
     * 핵심: @Override 추가하여 인터페이스 메서드 구현
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        try {
            List<User> users = jpaRepository.findAll();
            log.debug("전체 User 조회 완료: Count={}", users.size());
            return users;
        } catch (Exception e) {
            log.error("전체 User 조회 실패", e);
            throw new RuntimeException("전체 User 조회에 실패했습니다.", e);
        }
    }

    // =================================
    // 2. 페이징 조회 메서드 (완전히 수정됨)
    // =================================

    /**
     * 페이징된 User 조회
     *
     * 핵심 수정사항:
     * - Pageable.unpaged() 사용 금지
     * - 안전한 PageRequest로 변환
     * - getPageNumber() 호출 전 반드시 isPaged() 확인
     */
    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        // 1. null 처리 - Unpaged 대신 안전한 PageRequest 사용
        if (pageable == null) {
            log.debug("Pageable이 null이므로 대용량 페이징으로 변환");
            pageable = PageRequest.of(0, 10000); // 안전한 기본값
        }

        // 2. Unpaged 처리 - 안전한 PageRequest로 변환
        if (!pageable.isPaged()) {
            log.debug("Unpaged 요청이므로 대용량 페이징으로 변환");
            pageable = PageRequest.of(0, 10000, pageable.getSort());
        }

        try {
            // 3. 실제 조회 (이제 안전함)
            Page<User> result = jpaRepository.findAll(pageable);

            // 4. 안전한 로깅 (isPaged()가 true임이 보장됨)
            log.debug("페이징 User 조회 완료: Page={}, Size={}, TotalElements={}",
                    pageable.getPageNumber(), // 이제 안전
                    pageable.getPageSize(),
                    result.getTotalElements());

            return result;
        } catch (Exception e) {
            log.error("User 조회 실패", e);
            throw new RuntimeException("전체 User 조회에 실패했습니다.", e);
        }
    }

    // =================================
    // 3. 편의 메서드들
    // =================================

    /**
     * 페이지 번호와 크기로 조회하는 편의 메서드
     */
    @Transactional(readOnly = true)
    public Page<User> findAll(int page, int size) {
        if (page < 0 || size <= 0) {
            log.warn("잘못된 페이징 파라미터: page={}, size={}. 기본값으로 처리합니다.", page, size);
            page = 0;
            size = 20;
        }

        Pageable pageable = PageRequest.of(page, size);
        return findAll(pageable);
    }

    /**
     * 안전한 페이징 조회 - 예외 발생 시 전체 조회로 폴백
     */
    @Transactional(readOnly = true)
    public Page<User> findAllSafely(Pageable pageable) {
        try {
            return findAll(pageable);
        } catch (Exception e) {
            log.warn("페이징 조회 실패, 전체 조회로 폴백합니다.", e);
            List<User> allUsers = findAll(); // List 버전 호출
            return new PageImpl<>(allUsers, PageRequest.of(0, allUsers.size()), allUsers.size());
        }
    }

    // =================================
    // 4. 기존 메서드들 (수정 없음)
    // =================================

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User 객체는 필수입니다.");
        }

        try {
            User savedUser = jpaRepository.save(user);
            log.debug("User 저장 완료: ID={}, Email={}",
                    savedUser.getId(),
                    savedUser.getEmail() != null ? savedUser.getEmail().getValue() : "null");
            return savedUser;
        } catch (Exception e) {
            log.error("User 저장 실패: Email={}",
                    user.getEmail() != null ? user.getEmail().getValue() : "null", e);
            throw new RuntimeException("User 저장에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        if (id == null) {
            log.warn("User 조회 시도 - ID가 null입니다.");
            return Optional.empty();
        }

        try {
            return jpaRepository.findById(id);
        } catch (Exception e) {
            log.error("User 조회 실패: ID={}", id, e);
            throw new RuntimeException("User 조회에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(Email email) {
        if (email == null) {
            log.warn("User 이메일 조회 시도 - Email이 null입니다.");
            return Optional.empty();
        }

        try {
            return jpaRepository.findByEmail(email);
        } catch (Exception e) {
            log.error("User 이메일 조회 실패: Email={}", email.getValue(), e);
            throw new RuntimeException("User 이메일 조회에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(Email email) {
        if (email == null) {
            return false;
        }

        try {
            return jpaRepository.existsByEmail(email);
        } catch (Exception e) {
            log.error("User 이메일 존재 여부 확인 실패: Email={}", email.getValue(), e);
            throw new RuntimeException("User 이메일 존재 여부 확인에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findActiveUsers() {
        try {
            List<User> activeUsers = jpaRepository.findActiveUsers();
            log.debug("활성 User 조회 완료: Count={}", activeUsers.size());
            return activeUsers;
        } catch (Exception e) {
            log.error("활성 User 조회 실패", e);
            throw new RuntimeException("활성 User 조회에 실패했습니다.", e);
        }
    }

    @Override
    public void delete(User user) {
        if (user == null) {
            log.warn("User 삭제 시도 - 객체가 null입니다.");
            return;
        }

        try {
            jpaRepository.delete(user);
            log.info("User 삭제 완료: ID={}, Email={}",
                    user.getId(),
                    user.getEmail() != null ? user.getEmail().getValue() : "null");
        } catch (Exception e) {
            log.error("User 삭제 실패: ID={}, Email={}",
                    user.getId(),
                    user.getEmail() != null ? user.getEmail().getValue() : "null", e);
            throw new RuntimeException("User 삭제에 실패했습니다.", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            log.warn("User 삭제 시도 - ID가 null입니다.");
            return;
        }

        try {
            jpaRepository.deleteById(id);
            log.info("User 삭제 완료: ID={}", id);
        } catch (Exception e) {
            log.error("User 삭제 실패: ID={}", id, e);
            throw new RuntimeException("User 삭제에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }

        try {
            return jpaRepository.existsById(id);
        } catch (Exception e) {
            log.error("User 존재 여부 확인 실패: ID={}", id, e);
            throw new RuntimeException("User 존재 여부 확인에 실패했습니다.", e);
        }
    }
}