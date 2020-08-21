package com.keepreal.madagascar.tenrecs.factory.notificationMessageBuilder;

import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.tenrecs.NewAnswerNotice;
import com.keepreal.madagascar.tenrecs.NewQuestionNotice;
import com.keepreal.madagascar.tenrecs.NoticeNotificationMessage;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import com.keepreal.madagascar.tenrecs.factory.notificationBuilder.NotificationBuilder;
import com.keepreal.madagascar.tenrecs.model.Notice;
import com.keepreal.madagascar.tenrecs.model.Notification;

import java.util.Objects;

/**
 * Implements the {@link NotificationBuilder}.
 */
public class QuestionBoxNotificationMessageBuilder implements NotificationMessageBuilder{

    private long lastReadTimestamp;
    private Notification notification;

    /**
     * Sets the last read timestamp.
     *
     * @param lastReadTimestamp Last read comment notification timestamp.
     * @return this.
     */
    public QuestionBoxNotificationMessageBuilder setLastReadTimestamp(long lastReadTimestamp) {
        this.lastReadTimestamp = lastReadTimestamp;
        return this;
    }

    /**
     * Sets the notification.
     *
     * @param notification {@link Notification}.
     * @return this.
     */
    @Override
    public NotificationMessageBuilder setNotification(Notification notification) {
        this.notification = notification;
        return this;
    }

    /**
     * Builds the {@link NotificationMessage}.
     *
     * @return {@link NotificationMessage}.
     */
    @Override
    public NotificationMessage build() {
        if (Objects.isNull(this.notification)
                || !this.notification.getType().equals(NotificationType.NOTIFICATION_BOX_NOTICE)) {
            return null;
        }

        return NotificationMessage.newBuilder()
                .setId(this.notification.getId())
                .setType(NotificationType.NOTIFICATION_BOX_NOTICE)
                .setUserId(this.notification.getUserId())
                .setHasRead(this.notification.getCreatedAt().compareTo(this.lastReadTimestamp) < 0)
                .setNoticeNotification(this.toNoticeMessage(this.notification.getNotice()))
                .setTimestamp(this.notification.getTimestamp())
                .build();
    }

    /**
     * Converts {@link Notice} into {@link NoticeNotificationMessage}.
     *
     * @param notice {@link Notice}.
     * @return {@link NoticeNotificationMessage}.
     */
    private NoticeNotificationMessage toNoticeMessage(Notice notice) {
        if (Objects.isNull(notice)) {
            return null;
        }

        NoticeNotificationMessage.Builder noticeNotificationMessageBuilder = NoticeNotificationMessage.newBuilder()
                .setType(notice.getType());

        switch (notice.getType()) {
            case NOTICE_TYPE_BOX_NEW_QUESTION:
                if (Objects.isNull(notice.getNewQuestionNotice())) {
                    return noticeNotificationMessageBuilder.build();
                }

                NewQuestionNotice newQuestionNotice = NewQuestionNotice.newBuilder()
                        .setFeedId(notice.getNewQuestionNotice().getFeedId())
                        .setAuthorId(notice.getNewQuestionNotice().getAuthorId())
                        .build();
                noticeNotificationMessageBuilder.setNewQuestionNotice(newQuestionNotice);
                return noticeNotificationMessageBuilder.build();
            case NOTICE_TYPE_BOX_NEW_ANSWER:
                if (Objects.isNull(notice.getNewReplyNotice())) {
                    return noticeNotificationMessageBuilder.build();
                }

                NewAnswerNotice newAnswerNotice = NewAnswerNotice.newBuilder()
                        .setFeedId(notice.getNewReplyNotice().getFeedId())
                        .setAuthorId(notice.getNewReplyNotice().getAuthorId())
                        .build();

                noticeNotificationMessageBuilder.setNewAnswerNotice(newAnswerNotice);
                return noticeNotificationMessageBuilder.build();
            default:
        }

        return noticeNotificationMessageBuilder.build();
    }

}
