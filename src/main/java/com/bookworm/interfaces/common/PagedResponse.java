package com.bookworm.interfaces.common;

import lombok.Getter;
import org.springframework.data.domain.Page;

/**
 * 페이징 응답을 위한 DTO
 * Page 객체를 클라이언트 친화적인 형태로 변환
 */
@Getter
class PagedResponse<T> {
    // Getters
    private final java.util.List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    private PagedResponse(Page<T> page) {
        this.content = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
    }

    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(page);
    }

}
