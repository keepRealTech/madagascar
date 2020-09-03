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
 * Represents the balance appending log.
 */
@Builder
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "balance_log")
public class Payment {

    @Id
    private String id;
    private String userId;
    @Builder.Default
    private String payeeId = "";
    @Builder.Default
    private String tradeNum = "";
    @Builder.Default
    private Long amountInCents = 0L;
    @Builder.Default
    private Long amountInShells = 0L;
    @Builder.Default
    private Integer withdrawPercent = 99;
    @Builder.Default
    private String membershipSkuId = "";
    @Builder.Default
    private String orderId = "";
    private Integer type;
    @Builder.Default
    private Integer state = 1;
    @Builder.Default
    private Long validAfter = 0L;
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;

}
