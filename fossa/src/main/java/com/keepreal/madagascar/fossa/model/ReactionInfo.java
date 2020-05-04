package com.keepreal.madagascar.fossa.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Data
public class ReactionInfo {

    @Id
    private Long id;
    private Long feedId;
    private Long userId;
    private List<Integer> reactionTypeList;
    @Column(name = "is_deleted")
    private Boolean deleted;
    private Long createdTime;
    private Long updatedTime;
}
