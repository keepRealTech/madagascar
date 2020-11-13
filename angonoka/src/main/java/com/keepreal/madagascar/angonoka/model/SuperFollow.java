package com.keepreal.madagascar.angonoka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "super_follow")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SuperFollow {
    @Id
    private String id;
    private String platformId;
    private String hostId;
    private String islandId;
    @Builder.Default
    private Long lastPubTime = 0L;
    private Integer type;
    @Builder.Default
    private Integer state = 0;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updateTime;
}
