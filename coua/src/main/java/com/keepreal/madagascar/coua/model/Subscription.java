package com.keepreal.madagascar.coua.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Data
@Table(name = "subscription")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Subscription {

    @Id
    private Long id;
    private Long userId;
    private Long islandId;
    private Integer state;
    private Integer islanderNumber;
    @Column(name = "is_deleted")
    private Boolean deleted;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;
}
