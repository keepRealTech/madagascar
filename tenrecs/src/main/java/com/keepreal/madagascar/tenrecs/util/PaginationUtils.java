package com.keepreal.madagascar.tenrecs.util;

import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.PageResponse;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

/**
 * Represents the pagination utils.
 */
public class PaginationUtils {

    private static final String CREATED_AT_PROPERTY_NAME = "createdAt";

    @Getter
    private final static int DEFAULT_PAGE = 0;
    @Getter
    private final static int DEFAULT_PAGE_SIZE = 10;
    @Getter
    private final static Sort DEFAULT_SORT = Sort.by(Sort.Order.desc(PaginationUtils.CREATED_AT_PROPERTY_NAME));

    /**
     * Constructs a default {@link PageRequest}.
     *
     * @return {@link PageRequest}.
     */
    public static PageRequest defaultPageRequest() {
        return PageRequest.newBuilder()
                .setPage(PaginationUtils.DEFAULT_PAGE)
                .setPageSize(PaginationUtils.DEFAULT_PAGE_SIZE)
                .build();
    }

    /**
     * Converts the {@link PageRequest} into {@link org.springframework.data.domain.PageRequest}.
     *
     * @param pageRequest {@link PageRequest}.
     * @return {@link org.springframework.data.domain.PageRequest}.
     */
    public static org.springframework.data.domain.PageRequest valueOf(PageRequest pageRequest) {
        return org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage(),
                pageRequest.getPageSize(),
                PaginationUtils.DEFAULT_SORT);
    }

    /**
     * Builds the {@link PageResponse}.
     *
     * @param pageData    {@link Page}.
     * @param pageRequest {@link PageRequest}.
     * @return {@link PageResponse}.
     */
    public static PageResponse valueOf(Page<?> pageData, PageRequest pageRequest) {
        return PageResponse.newBuilder()
                .setPage(pageRequest.getPage())
                .setPageSize(pageRequest.getPageSize())
                .setHasContent(pageData.hasContent())
                .setHasMore(!pageData.isLast())
                .build();
    }

}
