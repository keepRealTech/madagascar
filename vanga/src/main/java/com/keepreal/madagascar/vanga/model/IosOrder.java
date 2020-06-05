package com.keepreal.madagascar.vanga.model;

import lombok.Builder;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Id;

public class IosOrder {

    @Id
    private String id;
    private String userId;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;

}
