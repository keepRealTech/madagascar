package com.keepreal.madagascar.angonoka.api;

public class WeiboApi {
    public static final String SHOW_USER_URL_BY_NAME = "https://c.api.weibo.com/2/users/show_batch/other.json?access_token=%s&screen_name=%s";
    public static final String SHOW_USER_URL_BY_UID = "https://api.weibo.com/2/users/show.json?access_token=%s&uid=%s";
    public static final String ADD_SUBSCRIBE = "https://c.api.weibo.com/subscribe/update_subscribe.json?source=%s&subid=%s&add_uids=%s";
    public static final String DELETE_SUBSCRIBE = "https://c.api.weibo.com/subscribe/update_subscribe.json?source=%s&subid=%s&del_uids=%s";
    public static final String COMMERCIAL_PUSH = "https://c.api.weibo.com/commercial/push?subid=%s";
}
