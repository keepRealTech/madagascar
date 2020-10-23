package com.keepreal.madagascar.coua.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
@Table(name = "support_target")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTarget {
    @Id
    private String id;
    private String islandId;
    private String hostId;
    private Integer targetType;
    private Integer timeType;
    @Builder.Default
    private String content = "";
    @Builder.Default
    private Long currentAmountInCents = 0L;
    @Builder.Default
    private Long totalAmountInCents = 0L;
    @Builder.Default
    private Long currentSupporterNum = 0L;
    @Builder.Default
    private Long totalSupporterNum = 0L;
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;
}
