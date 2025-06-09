package com.bookworm.domain.entity;

import com.bookworm.domain.common.AuditableBaseEntity;
import com.bookworm.domain.constant.BookStatus;
import com.bookworm.domain.vo.book.BookLanguage;
import com.bookworm.domain.vo.book.BookQuantity;
import com.bookworm.domain.vo.book.BookTitle;
import com.bookworm.domain.vo.book.BookType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

/**
 * 도서 엔티티
 * - 도서관의 도서 정보 관리
 * - 값 객체를 활용한 도메인 모델
 * - 도서 재고 관리 비즈니스 로직 포함
 */
@Entity
@Table(name = "books", indexes = {
        @Index(name = "idx_book_title", columnList = "title"),
        @Index(name = "idx_book_type", columnList = "type"),
        @Index(name = "idx_book_language", columnList = "language"),
        @Index(name = "idx_book_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class Book extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "title", nullable = false, length = 200))
    private BookTitle title;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "language", nullable = false, length = 20))
    private BookLanguage language;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "type", nullable = false, length = 30))
    private BookType type;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "quantity", nullable = false))
    private BookQuantity quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status;

    /**
     * 등록한 관리자 (누가 이 도서를 시스템에 등록했는지)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by", nullable = false)
    private User registeredBy;

    @Version
    private Long version;

    /**
     * 새로운 도서 생성
     */
    public static Book create(BookTitle title, BookLanguage language, BookType type,
                              BookQuantity quantity, User registeredBy) {
        validateCreateInputs(title, language, type, quantity, registeredBy);

        return Book.of()
                .title(title)
                .language(language)
                .type(type)
                .quantity(quantity)
                .status(BookStatus.AVAILABLE) // 기본 상태는 이용 가능
                .registeredBy(registeredBy)
                .build();
    }

    /**
     * 도서 생성 시 입력값 검증
     */
    private static void validateCreateInputs(BookTitle title, BookLanguage language,
                                             BookType type, BookQuantity quantity, User registeredBy) {
        if (title == null) {
            throw new IllegalArgumentException("도서 제목은 필수입니다.");
        }
        if (language == null) {
            throw new IllegalArgumentException("도서 언어는 필수입니다.");
        }
        if (type == null) {
            throw new IllegalArgumentException("도서 유형은 필수입니다.");
        }
        if (quantity == null) {
            throw new IllegalArgumentException("도서 수량은 필수입니다.");
        }
        if (registeredBy == null) {
            throw new IllegalArgumentException("등록자 정보는 필수입니다.");
        }
    }

    // 핵심 비즈니스 메서드

    /**
     * 도서 정보 수정
     */
    public void updateInfo(BookTitle newTitle, BookLanguage newLanguage,
                           BookType newType, BookQuantity newQuantity) {
        if (newTitle != null) {
            this.title = newTitle;
        }
        if (newLanguage != null) {
            this.language = newLanguage;
        }
        if (newType != null) {
            this.type = newType;
        }
        if (newQuantity != null) {
            this.quantity = newQuantity;
        }
    }

    /**
     * 재고 추가
     */
    public void addStock(Integer amount) {
        this.quantity = this.quantity.increase(amount);

        // 재고가 추가되면 상태를 이용 가능으로 변경
        if (this.status == BookStatus.BORROWED && this.quantity.hasStock()) {
            this.status = BookStatus.AVAILABLE;
        }
    }

    /**
     * 재고 감소 (대출 시)
     */
    public void borrowStock(Integer amount) {
        if (!canBorrow(amount)) {
            throw new IllegalStateException("대출할 수 없는 상태이거나 재고가 부족합니다.");
        }

        this.quantity = this.quantity.decrease(amount);

        // 재고가 0이 되면 대출 중 상태로 변경
        if (!this.quantity.hasStock()) {
            this.status = BookStatus.BORROWED;
        }
    }

    /**
     * 재고 반납
     */
    public void returnStock(Integer amount) {
        this.quantity = this.quantity.increase(amount);

        // 재고가 있으면 이용 가능 상태로 변경
        if (this.quantity.hasStock() && this.status == BookStatus.BORROWED) {
            this.status = BookStatus.AVAILABLE;
        }
    }

    /**
     * 도서 상태 변경
     */
    public void changeStatus(BookStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("도서 상태는 필수입니다.");
        }
        this.status = newStatus;
    }

    /**
     * 대출 가능 여부 확인
     */
    public boolean canBorrow(Integer amount) {
        return this.status == BookStatus.AVAILABLE && this.quantity.hasStock(amount);
    }

    /**
     * 이용 가능한 도서인지 확인
     */
    public boolean isAvailable() {
        return this.status == BookStatus.AVAILABLE && this.quantity.hasStock();
    }

    /**
     * 동일한 도서인지 확인 (제목, 언어, 유형이 모두 같으면 동일 도서)
     */
    public boolean isSameBook(BookTitle title, BookLanguage language, BookType type) {
        return this.title.equals(title) &&
                this.language.equals(language) &&
                this.type.equals(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        if (id == null || book.id == null) {
            return false;
        }

        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Book{id=%d, title='%s', language='%s', type='%s', quantity=%s, status=%s}",
                id, title, language, type, quantity, status);
    }
}