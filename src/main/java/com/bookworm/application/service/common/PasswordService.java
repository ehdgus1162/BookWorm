package com.bookworm.application.service.common;

import com.bookworm.domain.vo.user.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordService {
    private final PasswordEncoder passwordEncoder;

    public Password encrypt(String rawPassword) {
        return Password.of(rawPassword, passwordEncoder);
    }

    public boolean matches(String rawPassword, Password password) {
        return password.matches(rawPassword, passwordEncoder);
    }
}