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
    private String payerId;
    private String userId;
    private Long balanceChangeInCents = 0L;
    private Long balanceChangeInShells = 0L;
    private String orderId;
    private Integer type;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;

}
