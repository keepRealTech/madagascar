package com.keepreal.madagascar.fossa.model;

import lombok.Data;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Data
public class ReactionInfo {

    private Long id;
    private Long feedId;
    private Long userId;
    private Integer reactionType;
    private Long createTime;
    private Long updateTime;
}
