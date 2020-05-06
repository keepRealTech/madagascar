package com.keepreal.madagascar.coua.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Data
@Table(name = "island")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class IslandInfo {
    @Id
    private Long id;
    private Long hostId;
    private String islandName;
    private String portraitImageUri;
    private String description;
    private String secret;
    private Integer state;
    private Integer islanderNumber;
    private Long lastFeedAt; //用户客户端判断是否有未读feed消息（客户端轮询模式下使用）
    @Column(name = "is_deleted")
    private Boolean deleted;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;
}
