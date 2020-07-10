package com.keepreal.madagascar.marty.converter;

import com.keepreal.madagascar.common.PushPriority;
import com.keepreal.madagascar.marty.model.PushPriorityInfo;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-07-10
 **/

public class PushPriorityConverter {

    public static PushPriorityInfo convertTo(Integer priorityType) {
        PushPriorityInfo pushPriorityInfo = new PushPriorityInfo();

        switch (priorityType) {
            case PushPriority.NEW_COMMENT_VALUE:
                pushPriorityInfo.setName("评论");
                pushPriorityInfo.setAndroidUrl("");
                pushPriorityInfo.setIosUrl("");
                break;
            case PushPriority.NEW_LIKE_VALUE:
                pushPriorityInfo.setName("点赞");
                pushPriorityInfo.setAndroidUrl("");
                pushPriorityInfo.setIosUrl("");
                break;
            case PushPriority.NEW_MEMBERSHIP_VALUE:
                pushPriorityInfo.setName("新岛民");
                pushPriorityInfo.setAndroidUrl("");
                pushPriorityInfo.setIosUrl("");
                break;
            case PushPriority.NEW_SUBSCRIBE_VALUE:
                pushPriorityInfo.setName("新会员");
                pushPriorityInfo.setAndroidUrl("");
                pushPriorityInfo.setIosUrl("");
                break;
            default:
                System.out.println("");
        }

        return pushPriorityInfo;
    }
}
