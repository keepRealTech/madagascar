package com.keepreal.madagascar.angonoka.model;

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

@Data
@Table(name = "super_follow_subscription")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SuperFollowSubscription {
    @Id
    private String id;
    private String openId;
    private String unionId;
    private String hostId;
    private String platformId;
    private Integer type;
    @Column(name = "is_deleted")
    private boolean deleted;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updateTime;
}
