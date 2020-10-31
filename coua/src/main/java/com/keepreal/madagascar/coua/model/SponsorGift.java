package com.keepreal.madagascar.coua.model;

import com.keepreal.madagascar.common.enums.SponsorGiftType;
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
@Table(name = "sponsor_gift")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorGift {
    @Id
    private String id;
    private String uri;
    private String emoji;
    @Builder.Default
    private String name = "";
    @Builder.Default
    private String text = "";
    @Builder.Default
    private int type = 0;
    @Builder.Default
    private Boolean isDefault = false;
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;
}
