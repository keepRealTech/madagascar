package com.keepreal.madagascar.tenrecs.factory;

import com.keepreal.madagascar.tenrecs.NotificationMessage;
import com.keepreal.madagascar.tenrecs.factory.notificationMessageBuilder.CommentNotificationMessageBuilder;
import com.keepreal.madagascar.tenrecs.factory.notificationMessageBuilder.NoticeNotificationMessageBuilder;
import com.keepreal.madagascar.tenrecs.factory.notificationMessageBuilder.QuestionBoxNotificationMessageBuilder;
import com.keepreal.madagascar.tenrecs.factory.notificationMessageBuilder.ReactionNotificationMessageBuilder;
import com.keepreal.madagascar.tenrecs.model.Notification;
import com.keepreal.madagascar.tenrecs.model.UserNotificationRecord;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the notification message factory.
 */
@Component
public class NotificationMessageFactory {

    /**
     * Converts {@link Notification} into {@link NotificationMessage}.
     *
     * @param notification {@link Notification}.
     * @param record       {@link UserNotificationRecord}.
     * @return {@link NotificationMessage}.
     */
    public NotificationMessage toNotificationMessage(Notification notification,
                                                     UserNotificationRecord record) {
        if (Objects.isNull(notification)) {
            return null;
        }

        switch (notification.getType()) {
            case NOTIFICATION_COMMENTS:
                return new CommentNotificationMessageBuilder()
                        .setNotification(notification)
                        .setLastReadTimestamp(record.getLastReadCommentNotificationTimestamp())
                        .build();
            case NOTIFICATION_REACTIONS:
                return new ReactionNotificationMessageBuilder()
                        .setNotification(notification)
                        .setLastReadTimestamp(record.getLastReadReactionNotificationTimestamp())
                        .build();
            case NOTIFICATION_ISLAND_NOTICE:
                return new NoticeNotificationMessageBuilder()
                        .setNotification(notification)
                        .setLastReadTimestamp(record.getLastReadIslandNoticeNotificationTimestamp())
                        .build();
            case NOTIFICATION_BOX_NOTICE:
                return new QuestionBoxNotificationMessageBuilder()
                        .setLastReadTimestamp(Objects.isNull(record.getLastReadBoxNoticeNotificationTimestamp())
                                ? 0L : record.getLastReadBoxNoticeNotificationTimestamp())
                        .setNotification(notification)
                        .build();
            default:
                return null;
        }
    }

}
