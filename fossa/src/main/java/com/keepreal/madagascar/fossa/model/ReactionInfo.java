package com.keepreal.madagascar.fossa.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import java.util.Set;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Builder
@Data
@Document(value = "reactionInfo")
@EntityListeners(AuditingEntityListener.class)
public class ReactionInfo {

    @Id
    private String id;
    private String feedId;
    private String userId;
    private Set<Integer> reactionTypeList;
    @Builder.Default
    private Boolean deleted = false;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;

}
