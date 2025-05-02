package com.bookworm.domain.vo.value;

import com.bookworm.common.error.ErrorMessage;
import com.bookworm.domain.vo.exception.NameException;
import jakarta.persistence.Embeddable;

/**
 * 이름(FirstName) 값 객체
 */
@Embeddable
public class FirstName extends NamePart {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 30;

    /**
     * JPA를 위한 기본 생성자
     */
    protected FirstName() {
        super();
    }

    /**
     * 이름 값을 받아 객체 생성
     *
     * @param value 이름 문자열
     */
    public FirstName(String value) {
        super(value);
    }

    /**
     * 이름 객체 생성 팩토리 메서드
     *
     * @param value 이름 문자열
     * @return 이름 객체
     */
    public static FirstName of(String value) {
        return new FirstName(value);
    }

    /**
     * 이름 길이 검증 구현
     *
     * @param value 검증할 이름
     */
    @Override
    protected void validateLength(String value) {
        String trimmedValue = value.trim();
        if (trimmedValue.length() < MIN_LENGTH || trimmedValue.length() > MAX_LENGTH) {
            throw new NameException.InvalidNameLengthException(
                    String.format(ErrorMessage.Name.FIRST_NAME_LENGTH, MIN_LENGTH, MAX_LENGTH)
            );
        }
    }


}