package com.keepreal.madagascar.asity.model;

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

/**
 * Represents the chat group membership entity.
 */
@Data
@Table(name = "chat_group_membership")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatgroupMembership {

    @Id
    private String id;
    private String groupId;
    private String membershipId;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;

}
