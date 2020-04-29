package com.keepreal.madagascar.fossa.model;

import lombok.Data;

import javax.persistence.Column;

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
    @Column(name = "is_deleted")
    private Boolean deleted;
    private Long createdTime;
    private Long updatedTime;
}
