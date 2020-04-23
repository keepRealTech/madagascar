package com.keepreal.madagascar.fossa.model;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-23
 **/

@Document
public class UserInfo {
    private String name;
    private String imageUrl;

    public UserInfo() {
    }

    public UserInfo(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
