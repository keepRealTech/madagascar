package com.keepreal.madagascar.coua.util;

import com.keepreal.madagascar.common.PageResponse;
import org.springframework.data.domain.Page;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-05-06
 **/

public class PageResponseUtil {

    public static PageResponse buildResponse(Page page) {
        return PageResponse.newBuilder()
                .setPage(page.getNumber())
                .setPageSize(page.getSize())
                .setHasContent(page.hasContent())
                .setHasMore(!page.isLast())
                .build();
    }
}
