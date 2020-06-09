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
import javax.persistence.Version;

/**
 * Represents the user balance.
 */
@Builder
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "balance")
public class Balance {

    @Id
    private String id;
    private String userId;
    @Builder.Default
    private Long balanceInCents = 0L;
    @Builder.Default
    private Long balanceEligibleInCents = 0L;
    @Builder.Default
    private Long balanceInShells = 0L;
    @Builder.Default
    private Long withdrawDayLimitInCents = 2000000L;
    @Builder.Default
    private Integer withdrawPercent = 88;
    @Builder.Default
    private Boolean frozen = false;
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
    @Version
    private Long version;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;

}
