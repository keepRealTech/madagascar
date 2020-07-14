package com.keepreal.madagascar.marty.model;

import lombok.Data;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-07-10
 **/

@Data
public class PushPriorityInfo {

    private String text;
    private String androidUrl = "/noti/comment";
    private Integer iosUrl = 0;
    private String notificationType;
    private String notificationNoticeType;

}
