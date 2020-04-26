package com.keepreal.madagascar.lemur.util;

import com.keepreal.madagascar.common.PageResponse;
import swagger.model.PageInfo;

/**
 * Represents a set of pagination utils.
 */
public class PageInfoUtils {

    /**
     * Converts pageResponse into page info.
     *
     * @param pageResponse PageResponse
     * @return PageInfo
     */
    public static PageInfo getPageInfo(PageResponse pageResponse){
        if(pageResponse == null) {
            return null;
        }

        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageSize(pageResponse.getPageSize());
        pageInfo.setPage(pageResponse.getPage());
        pageInfo.setHasContent(pageResponse.getHasContent());
        pageInfo.setHasMore(pageResponse.getHasMore());
        return pageInfo;
    }

}
