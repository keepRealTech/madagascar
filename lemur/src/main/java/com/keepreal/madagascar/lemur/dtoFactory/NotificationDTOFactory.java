package com.keepreal.madagascar.lemur.dtoFactory;


import com.keepreal.madagascar.lemur.dtoFactory.notification.CommentNotificationDTOBuilder;
import com.keepreal.madagascar.lemur.dtoFactory.notification.NoticeNotificationDTOBuilder;
import com.keepreal.madagascar.lemur.dtoFactory.notification.ReactionNotificationDTOBuilder;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.UserService;
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
    private final UserService userService;
    private final UserDTOFactory userDTOFactory;
    private final IslandService islandService;
    private final IslandDTOFactory islandDTOFactory;

    /**
     * Constructs the notification dto factory.
     *
     * @param feedDTOFactory     {@link FeedDTOFactory}.
     * @param reactionDTOFactory {@link ReactionDTOFactory}.
     * @param commentDTOFactory  {@link CommentDTOFactory}.
     * @param userService        {@link UserService}.
     * @param userDTOFactory     {@link UserDTOFactory}.
     * @param islandService      {@link IslandService}.
     * @param islandDTOFactory   {@link IslandDTOFactory}.
     */
    public NotificationDTOFactory(FeedDTOFactory feedDTOFactory,
                                  ReactionDTOFactory reactionDTOFactory,
                                  CommentDTOFactory commentDTOFactory,
                                  UserService userService,
                                  UserDTOFactory userDTOFactory,
                                  IslandService islandService,
                                  IslandDTOFactory islandDTOFactory) {
        this.feedDTOFactory = feedDTOFactory;
        this.reactionDTOFactory = reactionDTOFactory;
        this.commentDTOFactory = commentDTOFactory;
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
        this.islandService = islandService;
        this.islandDTOFactory = islandDTOFactory;
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
                return new NoticeNotificationDTOBuilder()
                        .setNotificationMessage(notification)
                        .setIslandService(this.islandService)
                        .setIslandDTOFactory(this.islandDTOFactory)
                        .setUserService(this.userService)
                        .setUserDTOFactory(this.userDTOFactory)
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
