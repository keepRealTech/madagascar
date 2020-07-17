package com.keepreal.madagascar.marty.model;

import lombok.Data;

import java.util.Map;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-07-10
 **/

@Data
public class PushPriorityInfo {

    private String text;
    private String androidUrl = "/noti/comment";
    private String iosUrl;
    private String notificationType;
    private String notificationNoticeType;
    private Map<String, String> extrasMap;

}
