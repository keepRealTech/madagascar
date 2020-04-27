package com.keepreal.madagascar.fossa.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Data
public class FeedInfo {

    @Id
    private Long id;
    private Long islandId;
    private Long userId;
    private String content;
    private List<String> imageUrls;
    private Integer likesCount;
    private Integer commentsCount;
    private Integer repostCount;
    private Integer state;
    private Long createTime;
    private Long updateTime;
}
