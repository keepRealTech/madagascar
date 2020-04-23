package com.keepreal.madagascar.fossa.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-23
 **/

@Document("feedInfo")
public class FeedInfo {

    @Id
    private String id;
    private String islandId;
    private UserInfo userInfo;
    private String content;
    private List<String> imageUrls;
    private Integer likesCount;
    private Integer commentsCount;
    private Integer repostCount;
    private Integer state;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIslandId() {
        return islandId;
    }

    public void setIslandId(String islandId) {
        this.islandId = islandId;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    public Integer getRepostCount() {
        return repostCount;
    }

    public void setRepostCount(Integer repostCount) {
        this.repostCount = repostCount;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
