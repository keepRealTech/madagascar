package com.keepreal.madagascar.fossa.util;

import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-29
 **/

public class PageRequestResponseUtils {

    public static Pageable getPageableByRequest(PageRequest pageRequest) {
        int page = pageRequest.getPage();
        int pageSize = pageRequest.getPageSize();
        return org.springframework.data.domain.PageRequest.of(page, pageSize);
    }

    public static PageResponse buildPageResponse(Page page) {
        return PageResponse.newBuilder()
                .setPage(page.getNumber())
                .setPageSize(page.getSize())
                .setHasContent(page.hasContent())
                .setHasMore(!page.isLast())
                .build();
    }

    public static PageResponse buildPageResponse(int pageIndex, int pageSize, long totalElements) {
        return PageResponse.newBuilder()
                .setPage(pageIndex)
                .setPageSize(pageSize)
                .setHasContent(pageIndex * (pageSize+1) <= totalElements + pageSize)
                .setHasMore(totalElements / pageSize > pageIndex)
                .build();
    }
}
