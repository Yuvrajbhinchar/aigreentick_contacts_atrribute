package com.aigreentick.services.contacts.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Generic pagination wrapper
 */
@Data
@Builder
public class PageResponse<T> {

    private List<T> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private Boolean isFirst;
    private Boolean isLast;
}