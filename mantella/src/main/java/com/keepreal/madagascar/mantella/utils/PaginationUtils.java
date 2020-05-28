package com.keepreal.madagascar.mantella.utils;


import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Objects;

/**
 * Represents a set of pagination utils.
 */
public class PaginationUtils {

    @Getter
    private final static int DEFAULT_PAGE = 0;
    @Getter
    private final static int DEFAULT_PAGE_SIZE = 10;
    @Getter
    private final static Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("feedCreatedAt"));

    /**
     * Constructs a default {@link PageRequest}.
     *
     * @return {@link PageRequest}.
     */
    public static PageRequest defaultTimelinePageRequest(Integer pageSize) {
        return PageRequest.of(
                PaginationUtils.DEFAULT_PAGE,
                Objects.isNull(pageSize) ? PaginationUtils.DEFAULT_PAGE_SIZE : pageSize,
                PaginationUtils.DEFAULT_SORT);
    }

}
