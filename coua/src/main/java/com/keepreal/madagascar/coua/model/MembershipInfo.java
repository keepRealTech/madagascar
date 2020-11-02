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
@Table(name = "membership")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipInfo {

    @Id
    private String id;
    private String islandId;
    private String hostId;
    private String name;
    private String description;
    private Integer pricePerMonth;
    @Builder.Default
    private Boolean useCustomMessage = false;
    @Builder.Default
    private String message = "";
    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;
    @Column(name = "is_top")
    @Builder.Default
    private Boolean top = false;
    private Integer colorType;
    @Builder.Default
    private Integer memberCount = 0;
    @Column(name = "is_permanent")
    private Boolean permanent;
    private String imageUri;
    private Integer width;
    private Integer height;
    private Long size;
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;

}
