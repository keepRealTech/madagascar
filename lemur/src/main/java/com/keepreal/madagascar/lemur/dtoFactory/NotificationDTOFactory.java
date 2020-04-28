package com.keepreal.madagascar.lemur.dtoFactory;


import com.keepreal.madagascar.lemur.dtoFactory.notification.CommentNotificationDTOBuilder;
import com.keepreal.madagascar.lemur.dtoFactory.notification.IslandNoticeNotificationDTOBuilder;
import com.keepreal.madagascar.lemur.dtoFactory.notification.ReactionNotificationDTOBuilder;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import com.keepreal.madagascar.tenrecs.UnreadNotificationsCountMessage;
import org.springframework.stereotype.Component;
import swagger.model.NotificationDTO;
import swagger.model.UnreadNotificationCountDTO;

import java.util.Objects;

/**
 * Represents the notification dto factory.
 */
@Component
public class NotificationDTOFactory {

    private final FeedDTOFactory feedDTOFactory;
    private final ReactionDTOFactory reactionDTOFactory;
    private final CommentDTOFactory commentDTOFactory;

    public NotificationDTOFactory(FeedDTOFactory feedDTOFactory,
                                  ReactionDTOFactory reactionDTOFactory,
                                  CommentDTOFactory commentDTOFactory) {
        this.feedDTOFactory = feedDTOFactory;
        this.reactionDTOFactory = reactionDTOFactory;
        this.commentDTOFactory = commentDTOFactory;
    }

    /**
     * Converts {@link UnreadNotificationsCountMessage} to {@link UnreadNotificationCountDTO}.
     *
     * @param unreadNotificationsCountMessage {@link UnreadNotificationsCountMessage}.
     * @return {@link UnreadNotificationCountDTO}.
     */
    public UnreadNotificationCountDTO valueOf(UnreadNotificationsCountMessage unreadNotificationsCountMessage) {
        if (Objects.isNull(unreadNotificationsCountMessage)) {
            return null;
        }

        UnreadNotificationCountDTO countDTO = new UnreadNotificationCountDTO();
        countDTO.setUnreadCommentsCount(unreadNotificationsCountMessage.getUnreadCommentsCount());
        countDTO.setUnreadIslandNoticesCount(unreadNotificationsCountMessage.getUnreadIslandNoticesCount());
        countDTO.setUnreadReactionsCount(unreadNotificationsCountMessage.getUnreadReactionsCount());
        countDTO.setHasUnread(countDTO.getUnreadCommentsCount() > 0
                || countDTO.getUnreadIslandNoticesCount() > 0
                || countDTO.getUnreadReactionsCount() > 0);

        return countDTO;
    }

    /**
     * Converts {@link NotificationMessage} to {@link NotificationDTO}.
     *
     * @param notification {@link NotificationMessage}.
     * @return {@link NotificationDTO}.
     */
    public NotificationDTO valueOf(NotificationMessage notification) {
        if (Objects.isNull(notification)) {
            return null;
        }

        switch (notification.getType()) {
            case NOTIFICATION_ISLAND_NOTICE:
                return new IslandNoticeNotificationDTOBuilder()
                        .setNotificationMessage(notification)
                        .build();
            case NOTIFICATION_COMMENTS:
                return new CommentNotificationDTOBuilder()
                        .setNotificationMessage(notification)
                        .setFeedDTOFactory(this.feedDTOFactory)
                        .setCommentDTOFactory(this.commentDTOFactory)
                        .build();
            case NOTIFICATION_REACTIONS:
                return new ReactionNotificationDTOBuilder()
                        .setNotificationMessage(notification)
                        .setFeedDTOFactory(this.feedDTOFactory)
                        .setReactionDTOFactory(this.reactionDTOFactory)
                        .build();
            default:
                return null;
        }
    }

}
