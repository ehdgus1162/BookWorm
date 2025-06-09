package com.bookworm.infrastructure.security;

import com.bookworm.domain.entity.User;
import com.bookworm.domain.repository.UserRepository;
import com.bookworm.domain.vo.user.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security용 사용자 인증 서비스
 * User 엔티티를 기반으로 UserDetails를 생성하여 Spring Security에 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("사용자 인증 시도: {}", email);

        try {
            Email emailVo = Email.of(email);
            User user = userRepository.findByEmail(emailVo)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

            // 계정 활성화 상태 확인
            if (!user.isActive()) {
                log.warn("비활성화된 계정 로그인 시도: {}", email);
                throw new UsernameNotFoundException("비활성화된 계정입니다: " + email);
            }

            log.debug("사용자 인증 성공: {} (역할: {})", email, user.getRole());
            return new CustomUserDetails(user);

        } catch (Exception e) {
            log.error("사용자 인증 중 오류 발생: {}", email, e);
            throw new UsernameNotFoundException("사용자 인증에 실패했습니다: " + email, e);
        }
    }

    /**
     * Spring Security UserDetails 구현체
     * User 엔티티 정보를 Spring Security가 이해할 수 있는 형태로 변환
     */
    public static class CustomUserDetails implements UserDetails {

        private final User user;

        public CustomUserDetails(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // User의 Role을 Spring Security Authority로 변환
            return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }

        @Override
        public String getPassword() {
            // Password VO에서 실제 암호화된 비밀번호 반환
            return user.getPassword().getValue();
        }

        @Override
        public String getUsername() {
            // Email VO에서 이메일 값 반환
            return user.getEmail().getValue();
        }

        @Override
        public boolean isAccountNonExpired() {
            // 계정 만료 여부 (현재는 항상 true, 필요시 User 엔티티에 필드 추가)
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            // 계정 잠금 여부 (현재는 항상 true, 필요시 User 엔티티에 필드 추가)
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            // 비밀번호 만료 여부 (현재는 항상 true, 필요시 User 엔티티에 필드 추가)
            return true;
        }

        @Override
        public boolean isEnabled() {
            // 계정 활성화 상태
            return user.isActive();
        }

        /**
         * 원본 User 엔티티 반환
         */
        public User getUser() {
            return user;
        }

        /**
         * 사용자 ID 반환
         */
        public Long getUserId() {
            return user.getId();
        }

        /**
         * 사용자 전체 이름 반환
         */
        public String getFullName() {
            return user.getFullName();
        }

        /**
         * 사용자 역할 반환
         */
        public String getRole() {
            return user.getRole().name();
        }
    }
}