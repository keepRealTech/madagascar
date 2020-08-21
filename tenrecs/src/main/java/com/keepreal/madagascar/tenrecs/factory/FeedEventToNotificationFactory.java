package com.keepreal.madagascar.tenrecs.factory;

import com.keepreal.madagascar.common.NoticeType;
import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.mantella.FeedEventMessage;
import com.keepreal.madagascar.tenrecs.model.Notice;
import com.keepreal.madagascar.tenrecs.model.Notification;
import com.keepreal.madagascar.tenrecs.model.notice.NewQuestionNotice;
import com.keepreal.madagascar.tenrecs.model.notice.NewReplyNotice;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class FeedEventToNotificationFactory {

    /**
     * Converts {@link FeedEventMessage} into {@link Notification}.
     *
     * @param event {@link FeedEventMessage}.
     * @return {@link Notification}.
     */
    public Notification toNotification(FeedEventMessage event) {
        if (Objects.isNull(event)) {
            return null;
        }

        Notification.NotificationBuilder builder = Notification.builder()
                .eventId(event.getEventId())
                .timestamp(event.getTimestamp())
                .type(NotificationType.NOTIFICATION_BOX_NOTICE);

        switch (event.getType()) {
            case FEED_EVENT_CREATE:
                return builder.notice(Notice.builder()
                        .type(NoticeType.NOTICE_TYPE_BOX_NEW_QUESTION)
                        .newQuestionNotice(NewQuestionNotice.builder().feedId(event.getFeedCreateEvent().getFeedId())
                                .authorId(event.getFeedCreateEvent().getAuthorId()).build())
                        .build())
                        .userId("event.getFeedCreateEvent().getHostId()")
                        .build();
            case FEED_EVENT_UPDATE:
                return builder.notice(Notice.builder()
                        .type(NoticeType.NOTICE_TYPE_BOX_NEW_ANSWER)
                        .newReplyNotice(NewReplyNotice.builder().feedId(event.getFeedUpdateEvent().getFeedId())
                                .authorId(event.getFeedCreateEvent().getAuthorId()).build())
                        .build())
                        .userId(event.getFeedUpdateEvent().getAuthorId())
                        .build();
            default:
                return null;
        }
    }

}
