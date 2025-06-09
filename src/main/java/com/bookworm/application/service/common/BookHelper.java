package com.bookworm.application.service.common;

import com.bookworm.domain.entity.*;
import com.bookworm.domain.vo.user.*;
import com.bookworm.domain.exception.BookBusinessException;
import com.bookworm.domain.repository.BookRepository;
import com.bookworm.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 도서 관련 공통 헬퍼
 * - 중복된 조회 + 예외처리 로직 통합
 */
@Component
@RequiredArgsConstructor
public class BookHelper {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /**
     * ID로 도서 조회, 없으면 예외 발생
     */
    public Book getBookOrThrow(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookBusinessException("도서를 찾을 수 없습니다: " + bookId));
    }

    /**
     * 이메일로 사용자 조회, 없으면 예외 발생
     */
    public User getUserOrThrow(String email) {
        Email emailVo = Email.of(email);
        return userRepository.findByEmail(emailVo)
                .orElseThrow(() -> new BookBusinessException("사용자를 찾을 수 없습니다: " + email));
    }
}