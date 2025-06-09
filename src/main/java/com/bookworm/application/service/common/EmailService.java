package com.bookworm.application.service.common;

import com.bookworm.domain.constant.ErrorMessages;
import com.bookworm.domain.exception.DuplicateUserException;
import com.bookworm.domain.repository.UserRepository;
import com.bookworm.domain.vo.user.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailService {
    private final UserRepository userRepository;

    public void checkDoesNotExist(Email email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateUserException(ErrorMessages.Duplication.EMAIL_EXISTS);
        }
    }
}