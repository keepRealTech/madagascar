package com.keepreal.madagascar.mantella.utils;

import com.keepreal.madagascar.common.PageRequest;

/**
 * Represents a set of pagination utils.
 */
public class PaginationUtils {

    /**
     * Constructs a {@link PageRequest}.
     *
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link PageRequest}.
     */
    public static PageRequest buildPageRequest(int page, int pageSize) {
        return PageRequest.newBuilder()
                .setPage(page)
                .setPageSize(pageSize)
                .build();
    }

}
