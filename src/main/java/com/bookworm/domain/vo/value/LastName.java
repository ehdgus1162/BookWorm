package com.bookworm.domain.vo.value;

import com.bookworm.common.error.ErrorMessage;
import com.bookworm.domain.vo.exception.NameException;
import jakarta.persistence.Embeddable;

/**
 * 성(LastName) 값 객체
 */
@Embeddable
public class LastName extends NamePart {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 20;

    /**
     * JPA를 위한 기본 생성자
     */
    protected LastName() {
        super();
    }

    /**
     * 성 값을 받아 객체 생성
     *
     * @param value 성 문자열
     */
    public LastName(String value) {
        super(value);
    }

    /**
     * 성 객체 생성 팩토리 메서드
     *
     * @param value 성 문자열
     * @return 성 객체
     */
    public static LastName of(String value) {
        return new LastName(value);
    }

    /**
     * 성 길이 검증 구현
     *
     * @param value 검증할 성
     */
    @Override
    protected void validateLength(String value) {
        String trimmedValue = value.trim();
        if (trimmedValue.length() < MIN_LENGTH || trimmedValue.length() > MAX_LENGTH) {
            throw new NameException.InvalidNameLengthException(
                    String.format(ErrorMessage.Name.LAST_NAME_LENGTH, MIN_LENGTH, MAX_LENGTH)
            );
        }
    }
}