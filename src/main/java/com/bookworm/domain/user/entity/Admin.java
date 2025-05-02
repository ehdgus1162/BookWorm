package com.bookworm.domain.user.entity;

import com.bookworm.domain.vo.value.Email;
import com.bookworm.domain.vo.value.FullName;
import com.bookworm.domain.vo.value.Password;
import com.bookworm.infrastructure.jpa.BaseUserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admins")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends BaseUserEntity {

    /**
     * 사용자 생성자
     *
     * @param name 사용자 이름
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호 (암호화되지 않은 상태)
     */
    public Admin(FullName name, Email email, Password password) {
        super(name, email, password);
    }

    /**
     * 사용자 생성 팩토리 메서드
     *
     * @param lastName 성
     * @param firstName 이름
     * @param email 이메일
     * @param password 비밀번호
     * @return 새로운 사용자 엔티티
     */
    public static Admin of(String lastName, String firstName, String email, String password) {
        return new Admin(
                FullName.of(lastName, firstName),
                Email.of(email),
                Password.of(password)
        );
    }

}
