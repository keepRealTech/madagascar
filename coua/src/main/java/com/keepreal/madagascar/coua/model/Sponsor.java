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
@Table(name = "sponsor")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sponsor {
    @Id
    private String id;
    private String hostId;
    private String islandId;
    @Builder.Default
    private String description = "";
    private String giftId;
    @Builder.Default
    private Long pricePerUnit = 0L;
    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;
}
