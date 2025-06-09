package com.bookworm.application.service.common;


import com.bookworm.application.dto.BookPageResponse;
import com.bookworm.application.dto.BookResponse;
import com.bookworm.application.dto.CreateBookRequest;
import com.bookworm.application.dto.UpdateBookRequest;
import com.bookworm.domain.entity.Book;
import com.bookworm.domain.vo.book.BookLanguage;
import com.bookworm.domain.vo.book.BookQuantity;
import com.bookworm.domain.vo.book.BookTitle;
import com.bookworm.domain.vo.book.BookType;
import org.mapstruct.Mapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;


import java.util.List;

/**
 * MapStruct 기반 도서 매퍼
 * - DTO ↔ Entity 변환 로직 완전 분리
 * - 타입 안전한 매핑
 */
@Mapper(componentModel = "spring")
public interface BookMapper {

    /**
     * CreateBookRequest → 값 객체들로 변환
     */
    @Mapping(target = "title", source = "title", qualifiedByName = "stringToBookTitle")
    @Mapping(target = "language", source = "language", qualifiedByName = "stringToBookLanguage")
    @Mapping(target = "type", source = "type", qualifiedByName = "stringToBookType")
    @Mapping(target = "quantity", source = "quantity", qualifiedByName = "integerToBookQuantity")
    BookCreationValues toBookCreationValues(CreateBookRequest request);

    /**
     * UpdateBookRequest → 값 객체들로 변환
     */
    @Mapping(target = "title", source = "title", qualifiedByName = "stringToBookTitle")
    @Mapping(target = "language", source = "language", qualifiedByName = "stringToBookLanguage")
    @Mapping(target = "type", source = "type", qualifiedByName = "stringToBookType")
    @Mapping(target = "quantity", source = "quantity", qualifiedByName = "integerToBookQuantity")
    BookUpdateValues toBookUpdateValues(UpdateBookRequest request);

    /**
     * Book Entity → BookResponse DTO
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title.value")
    @Mapping(target = "language", source = "language.value")
    @Mapping(target = "type", source = "type.value")
    @Mapping(target = "quantity", source = "quantity.value")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "statusDescription", source = "status.description")
    @Mapping(target = "registeredBy", source = "registeredBy.fullName")
    @Mapping(target = "registeredAt", source = "createdAt")
    @Mapping(target = "isAvailable", expression = "java(book.isAvailable())")
    @Mapping(target = "canBorrow", expression = "java(book.canBorrow(1))")
    BookResponse toBookResponse(Book book);

    /**
     * Book 리스트 → BookResponse 리스트
     */
    List<BookResponse> toBookResponseList(List<Book> books);

    /**
     * Page<Book> → BookPageResponse
     */
    default BookPageResponse toBookPageResponse(Page<Book> bookPage) {
        List<BookResponse> content = toBookResponseList(bookPage.getContent());
        return new BookPageResponse(
                content,
                bookPage.getTotalElements(),
                bookPage.getTotalPages(),
                bookPage.getNumber(),
                bookPage.getSize(),
                bookPage.hasNext(),
                bookPage.hasPrevious()
        );
    }

    // === 값 객체 변환 메서드들 ===

    @Named("stringToBookTitle")
    default BookTitle stringToBookTitle(String title) {
        return title != null ? BookTitle.of(title) : null;
    }

    @Named("stringToBookLanguage")
    default BookLanguage stringToBookLanguage(String language) {
        return language != null ? BookLanguage.of(language) : null;
    }

    @Named("stringToBookType")
    default BookType stringToBookType(String type) {
        return type != null ? BookType.of(type) : null;
    }

    @Named("integerToBookQuantity")
    default BookQuantity integerToBookQuantity(Integer quantity) {
        return quantity != null ? BookQuantity.of(quantity) : null;
    }
}