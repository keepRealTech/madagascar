package com.keepreal.madagascar.fossa.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Data
@Document(value = "reactionInfo")
@EntityListeners(AuditingEntityListener.class)
public class ReactionInfo {

    @Id
    private String id;
    private String feedId;
    private String userId;
    private List<Integer> reactionTypeList;
    @Column(name = "is_deleted")
    private Boolean deleted;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;
}
