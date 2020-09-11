package com.keepreal.madagascar.hoopoe.model;

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

@Data
@Table(name = "activity")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Activity {

    @Id
    private String id;
    @Builder.Default
    private String imageUri = "";
    @Builder.Default
    private String redirectUrl = "";
    @Builder.Default
    private Integer type = 0;
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updateTime;

}
