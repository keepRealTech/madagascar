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
 * Represents the island identity entity.
 */
@Data
@Table(name = "island_identity")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class IslandIdentity {

    @Id
    private String id;
    private String name;
    @Builder.Default
    private String iconImageUri = "";
    @Builder.Default
    private String description = "";
    @Builder.Default
    @Column(name = "is_active")
    private Boolean active = true;
    @Builder.Default
    private String startColor = "";
    @Builder.Default
    private String endColor = "";
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;

}
