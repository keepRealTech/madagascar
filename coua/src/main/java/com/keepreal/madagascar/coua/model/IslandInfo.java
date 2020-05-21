package com.keepreal.madagascar.coua.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class IslandInfo {
    @Id
    private String id;
    private String hostId;
    private String islandName;
    @Builder.Default
    private String portraitImageUri = "";
    @Builder.Default
    private String description = "";
    @Builder.Default
    private String secret = "";
    @Builder.Default
    private Integer state = 0;
    @Builder.Default
    private Integer islanderNumber = 1;
    @Builder.Default
    private Long lastFeedAt = 0L; //用户客户端判断是否有未读feed消息（客户端轮询模式下使用）
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;
}
