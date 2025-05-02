package com.bookworm.domain.vo.validator;

import com.bookworm.common.error.ErrorMessage;
import com.bookworm.domain.vo.exception.EmailException;

import java.util.regex.Pattern;

public class EmailValidatorImpl implements EmailValidator {
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    /**
     * 이메일 형식 유효성 검증
     */
    @Override
    public void validate(String email) {

        if (email == null || email.trim().isBlank()) {
            throw new EmailException.EmptyEmailException(ErrorMessage.Email.EMPTY);
        }

        if (!pattern.matcher(email).matches()) {
            throw new EmailException.InvalidEmailFormatException(ErrorMessage.Email.INVALID);
        }

        String[] parts = email.split("@");
        if (parts.length > 1 && parts[1].length() > 255) {
            throw new EmailException.InvalidEmailFormatException(ErrorMessage.Email.DOMAIN_TOO_LONG);
        }
    }


}
