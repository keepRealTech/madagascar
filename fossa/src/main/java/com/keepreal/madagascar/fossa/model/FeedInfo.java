package com.keepreal.madagascar.fossa.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Data
@Document(value = "feedInfo")
public class FeedInfo {

    @Id
    private Long id;
    private Long islandId;
    private Long userId;
    private String text;
    private List<String> imageUrls;
    private Boolean fromHost;
    private Integer likesCount;
    private Integer commentsCount;
    private Integer repostCount;
    private Integer state;
    private boolean deleted;
    private Long createdTime;
    private Long updatedTime;
}
