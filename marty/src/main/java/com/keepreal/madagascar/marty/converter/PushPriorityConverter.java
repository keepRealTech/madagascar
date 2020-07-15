package com.keepreal.madagascar.marty.converter;

import com.keepreal.madagascar.common.PushPriority;
import com.keepreal.madagascar.marty.model.PushPriorityInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-07-10
 **/

public class PushPriorityConverter {

    public static PushPriorityInfo convertTo(Integer priorityType) {
        PushPriorityInfo pushPriorityInfo = new PushPriorityInfo();
        Map<String, String> extrasMap = new HashMap<>();

        switch (priorityType) {
            case PushPriority.NEW_FEED_VALUE:
                pushPriorityInfo.setAndroidUrl("/feed/detail");
                pushPriorityInfo.setIosUrl(1);
                pushPriorityInfo.setText("更新了一条动态");
                pushPriorityInfo.setExtrasMap(extrasMap);
                break;
            case PushPriority.NEW_COMMENT_VALUE:
                pushPriorityInfo.setText("评论了你的动态");
                extrasMap.put("notification_type", "NOTIFICATION_COMMENTS");
                pushPriorityInfo.setExtrasMap(extrasMap);
                break;
            case PushPriority.NEW_LIKE_VALUE:
                pushPriorityInfo.setText("赞了你的动态");
                extrasMap.put("notification_type", "NOTIFICATION_REACTIONS");
                pushPriorityInfo.setExtrasMap(extrasMap);
                break;
            case PushPriority.NEW_MEMBERSHIP_VALUE:
                pushPriorityInfo.setText("加入了你的岛");
                extrasMap.put("notification_type", "NOTIFICATION_ISLAND_NOTICE");
                extrasMap.put("notification_notice_type", "SUBSCRIBER");
                pushPriorityInfo.setExtrasMap(extrasMap);
                break;
            case PushPriority.NEW_SUBSCRIBE_VALUE:
                pushPriorityInfo.setText("订阅了你的会员");
                extrasMap.put("notification_type", "NOTIFICATION_ISLAND_NOTICE");
                extrasMap.put("notification_notice_type", "MEMBER");
                pushPriorityInfo.setExtrasMap(extrasMap);
                break;
            default:
                return pushPriorityInfo;
        }

        return pushPriorityInfo;
    }
}
