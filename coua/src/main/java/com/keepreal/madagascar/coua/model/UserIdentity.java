package com.keepreal.madagascar.coua.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Data
@Table(name = "user_identity")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class UserIdentity {

    @Id
    private Long id;
    private Long userId;
    private Integer identityType;
    @Column(name = "is_deleted")
    private Boolean deleted;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;
}
