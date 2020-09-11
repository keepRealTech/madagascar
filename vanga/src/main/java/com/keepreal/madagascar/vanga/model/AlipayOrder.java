package com.keepreal.madagascar.vanga.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Transient;

@Builder
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AlipayOrder implements Order {

    @Id
    private String id;
    private String userId;
    private String description;
    private String tradeNumber;
    private String feeInCents;
    private Integer type;
    @Builder.Default
    private String propertyId = "";
    private String appId;
    @Transient
    private String orderString;
    @Transient
    private String mwebUrl;
    @Builder.Default
    private String transactionId = "";
    @Builder.Default
    private Integer state = 0;
    @Builder.Default
    private String errorMessage = "";
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;

}
