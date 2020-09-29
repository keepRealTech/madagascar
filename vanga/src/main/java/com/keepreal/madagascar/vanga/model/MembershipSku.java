package com.keepreal.madagascar.vanga.model;

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
 * Represents the membership sku.
 */
@Builder
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "membershipSku")
public class MembershipSku {

    @Id
    private String id;
    private String membershipId;
    private String membershipName;
    private String hostId;
    private String islandId;
    private String appleSkuId;
    @Builder.Default
    private String description = "";
    @Builder.Default
    private Long priceInShells = 0L;
    @Builder.Default
    private Long priceInCents = 0L;
    @Builder.Default
    private Integer timeInMonths = 1;
    @Column(name = "is_default")
    @Builder.Default
    private Boolean defaultSku = false;
    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;
    @Column(name = "is_permanent")
    @Builder.Default
    private Boolean permanent = false;
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;

}