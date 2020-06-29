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
     * Converts has more into page info.
     *
     * @param hasContent  Whether has content.
     * @param hasMore     Whether has more.
     * @param pageSize    Page size.
     * @return {@link PageInfo}.
     */
    public static PageInfo getPageInfo(boolean hasContent, boolean hasMore, int pageSize) {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageSize(pageSize);
        pageInfo.setPage(0);
        pageInfo.setHasContent(hasContent);
        pageInfo.setHasMore(hasMore);
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
