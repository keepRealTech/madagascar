package com.keepreal.madagascar.lemur.dtoFactory;


import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.config.SystemNotificationConfiguration;
import com.keepreal.madagascar.lemur.dtoFactory.notificationBuilder.CommentNotificationDTOBuilder;
import com.keepreal.madagascar.lemur.dtoFactory.notificationBuilder.NoticeNotificationDTOBuilder;
import com.keepreal.madagascar.lemur.dtoFactory.notificationBuilder.ReactionNotificationDTOBuilder;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import com.keepreal.madagascar.tenrecs.UnreadNotificationsCountMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import swagger.model.NotificationDTO;
import swagger.model.NotificationType;
import swagger.model.SystemNoticeDTO;
import swagger.model.SystemNotificationDTO;
import swagger.model.UnreadNotificationCountDTO;

import java.util.Objects;

/**
 * Represents the notification dto factory.
 */
@Slf4j
@Component
public class NotificationDTOFactory {

    private final FeedDTOFactory feedDTOFactory;
    private final ReactionDTOFactory reactionDTOFactory;
    private final CommentDTOFactory commentDTOFactory;
    private final UserService userService;
    private final UserDTOFactory userDTOFactory;
    private final IslandService islandService;
    private final IslandDTOFactory islandDTOFactory;
    private final SystemNotificationConfiguration systemNotificationConfiguration;

    /**
     * Constructs the notification dto factory.
     *
     * @param feedDTOFactory                  {@link FeedDTOFactory}.
     * @param reactionDTOFactory              {@link ReactionDTOFactory}.
     * @param commentDTOFactory               {@link CommentDTOFactory}.
     * @param userService                     {@link UserService}.
     * @param userDTOFactory                  {@link UserDTOFactory}.
     * @param islandService                   {@link IslandService}.
     * @param islandDTOFactory                {@link IslandDTOFactory}.
     * @param systemNotificationConfiguration {@link SystemNotificationConfiguration}.
     */
    public NotificationDTOFactory(FeedDTOFactory feedDTOFactory,
                                  ReactionDTOFactory reactionDTOFactory,
                                  CommentDTOFactory commentDTOFactory,
                                  UserService userService,
                                  UserDTOFactory userDTOFactory,
                                  IslandService islandService,
                                  IslandDTOFactory islandDTOFactory,
                                  SystemNotificationConfiguration systemNotificationConfiguration) {
        this.feedDTOFactory = feedDTOFactory;
        this.reactionDTOFactory = reactionDTOFactory;
        this.commentDTOFactory = commentDTOFactory;
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
        this.islandService = islandService;
        this.islandDTOFactory = islandDTOFactory;
        this.systemNotificationConfiguration = systemNotificationConfiguration;
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

        try {
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
                case NOTIFICATION_SYSTEM_NOTICE:
                    SystemNoticeDTO systemNotice = new SystemNoticeDTO();
                    systemNotice.setName(this.systemNotificationConfiguration.getName());
                    systemNotice.setContent(this.systemNotificationConfiguration.getContent());
                    systemNotice.setPortraitImageUri(this.systemNotificationConfiguration.getPortraitImageUri());

                    NotificationDTO systemNotificationDTO = new NotificationDTO();
                    systemNotificationDTO.setNotificationType(NotificationType.SYSTEM_NOTICE);
                    systemNotificationDTO.setSystemNotice(systemNotice);

                    return systemNotificationDTO;
                default:
                    return null;
            }
        } catch (KeepRealBusinessException e) {
            log.error("Build NotificationDTO {} exception, message is {}", notification.getId(), e.getMessage());
            return null;
        }
    }

}
