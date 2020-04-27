package com.keepreal.madagascar.fossa.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Data
public class CommentInfo {

    @Id
    private Long id;
    private Long feedId;
    private String content;
    private Long replyToId;
    private Boolean deleted;
    private Long createTime;
    private Long updateTime;
}
