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
                pushPriorityInfo.setIosUrl("feeds://detail?id=");
                pushPriorityInfo.setText("更新了一条动态");
                pushPriorityInfo.setExtrasMap(extrasMap);
                break;
            case PushPriority.NEW_COMMENT_VALUE:
                pushPriorityInfo.setText("评论了你的动态");
                pushPriorityInfo.setIosUrl("message://comment");
                extrasMap.put("notification_type", "NOTIFICATION_COMMENTS");
                pushPriorityInfo.setExtrasMap(extrasMap);
                break;
            case PushPriority.NEW_LIKE_VALUE:
                pushPriorityInfo.setText("赞了你的动态");
                pushPriorityInfo.setIosUrl("message://action");
                extrasMap.put("notification_type", "NOTIFICATION_REACTIONS");
                pushPriorityInfo.setExtrasMap(extrasMap);
                break;
            case PushPriority.NEW_MEMBERSHIP_VALUE:
                pushPriorityInfo.setText("刚刚支持了你");
                pushPriorityInfo.setIosUrl("message://vip");
                extrasMap.put("notification_type", "NOTIFICATION_ISLAND_NOTICE");
                extrasMap.put("notification_notice_type", "MEMBER");
                pushPriorityInfo.setExtrasMap(extrasMap);
                break;
            case PushPriority.NEW_SUBSCRIBE_VALUE:
                pushPriorityInfo.setText("加入了你的岛");
                pushPriorityInfo.setIosUrl("message://island");
                extrasMap.put("notification_type", "NOTIFICATION_ISLAND_NOTICE");
                extrasMap.put("notification_notice_type", "SUBSCRIBER");
                pushPriorityInfo.setExtrasMap(extrasMap);
                break;
            case PushPriority.NEW_QUESTION_VALUE:
                pushPriorityInfo.setText("你收到了一个提问，立即查看");
                pushPriorityInfo.setAndroidUrl("/flutter/notification_question_box");
                pushPriorityInfo.setIosUrl("message://ask");
                extrasMap.put("notification_type", "NOTIFICATION_QUESTIONBOX");
                extrasMap.put("notification_box_type", "QUESTION");
                pushPriorityInfo.setExtrasMap(extrasMap);
                break;
            case PushPriority.NEW_REPLY_VALUE:
                pushPriorityInfo.setText("你收到了一个回答，立即查看");
                pushPriorityInfo.setAndroidUrl("/flutter/notification_question_box");
                pushPriorityInfo.setIosUrl("message://answer");
                extrasMap.put("notification_type", "NOTIFICATION_QUESTIONBOX");
                extrasMap.put("notification_box_type", "REPLY");
                pushPriorityInfo.setExtrasMap(extrasMap);
                break;
            case PushPriority.NEW_PUBLIC_REPLY_VALUE:
                pushPriorityInfo.setText("刚刚回答了一个问题，速来围观！");
                pushPriorityInfo.setAndroidUrl("/feed/detail");
                pushPriorityInfo.setIosUrl("message://answerDetail");
                extrasMap.put("notification_type", "NOTIFICATION_QUESTIONBOX");
                extrasMap.put("notification_box_type", "REPLY");
                pushPriorityInfo.setExtrasMap(extrasMap);
                break;
            default:
                return pushPriorityInfo;
        }

        return pushPriorityInfo;
    }
}
