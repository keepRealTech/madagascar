package com.keepreal.madagascar.tenrecs.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents the model that stores the latest read notification timestamp for a user.
 */
@Builder
@Data
@Document("userNotificationRecord")
public class UserNotificationRecord {

    @Id
    private String id;
    private String userId;
    @Builder.Default
    private Long lastReadCommentNotificationTimestamp = 0L;
    @Builder.Default
    private Long lastReadReactionNotificationTimestamp = 0L;
    @Builder.Default
    private Long lastReadIslandNoticeNotificationTimestamp = 0L;
    @Builder.Default
    private Long lastReadIslandNoticeNewSubscriberNotificationTimestamp = 0L;
    @Builder.Default
    private Long lastReadIslandNoticeNewMemberNotificationTimestamp = 0L;
    @Builder.Default
    private Long lastReadBoxNoticeNotificationTimestamp = 0L;
    @Builder.Default
    private Long lastReadBoxNoticeNewQuestionNotificationTimestamp = 0L;
    @Builder.Default
    private Long lastReadBoxNoticeNewReplyNotificationTimestamp = 0L;
    @Builder.Default
    private Boolean isDeleted = false;
    @CreatedDate
    private Long createdAt;
    @LastModifiedDate
    private Long updatedAt;

}
