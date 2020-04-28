package com.keepreal.madagascar.lemur.util;

import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.PageResponse;
import swagger.model.PageInfo;

/**
 * Represents a set of pagination utils.
 */
public class PaginationUtils {

    /**
     * Converts pageResponse into page info.
     *
     * @param pageResponse PageResponse
     * @return PageInfo
     */
    public static PageInfo getPageInfo(PageResponse pageResponse) {
        if (pageResponse == null) {
            return null;
        }

        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageSize(pageResponse.getPageSize());
        pageInfo.setPage(pageResponse.getPage());
        pageInfo.setHasContent(pageResponse.getHasContent());
        pageInfo.setHasMore(pageResponse.getHasMore());
        return pageInfo;
    }

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
