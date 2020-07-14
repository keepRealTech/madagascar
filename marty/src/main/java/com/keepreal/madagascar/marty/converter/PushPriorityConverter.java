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
                pushPriorityInfo.setText("评论了你的动态");
                pushPriorityInfo.setNotificationType("type：NOTIFICATION_COMMENTS");
                break;
            case PushPriority.NEW_LIKE_VALUE:
                pushPriorityInfo.setText("赞了你的动态");
                pushPriorityInfo.setNotificationType("NOTIFICATION_REACTIONS");
                break;
            case PushPriority.NEW_MEMBERSHIP_VALUE:
                pushPriorityInfo.setText("加入了你的岛");
                pushPriorityInfo.setNotificationType("NOTIFICATION_ISLAND_NOTICE");
                pushPriorityInfo.setNotificationNoticeType("SUBSCRIBER");
                break;
            case PushPriority.NEW_SUBSCRIBE_VALUE:
                pushPriorityInfo.setText("订阅了你的会员");
                pushPriorityInfo.setNotificationType("NOTIFICATION_ISLAND_NOTICE");
                pushPriorityInfo.setNotificationNoticeType("MEMBER");
                break;
            default:
                return pushPriorityInfo;
        }

        return pushPriorityInfo;
    }
}
