package com.keepreal.madagascar.lemur.dtoFactory;


import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.config.SystemNotificationConfiguration;
import com.keepreal.madagascar.lemur.dtoFactory.notificationBuilder.CommentNotificationDTOBuilder;
import com.keepreal.madagascar.lemur.dtoFactory.notificationBuilder.NoticeNotificationDTOBuilder;
import com.keepreal.madagascar.lemur.dtoFactory.notificationBuilder.QuestionBoxNotificationDTOBuilder;
import com.keepreal.madagascar.lemur.dtoFactory.notificationBuilder.ReactionNotificationDTOBuilder;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import com.keepreal.madagascar.tenrecs.UnreadNotificationsCountMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import swagger.model.NotificationDTO;
import swagger.model.NotificationType;
import swagger.model.SystemNoticeDTO;
import swagger.model.UnreadIslandNoticesCountDTO;
import swagger.model.UnreadNotificationCountDTO;
import swagger.model.UnreadNotificationCountDTOV2;
import swagger.model.UnreadQuestsionNoticesCountDTO;

import java.util.Objects;

/**
 * Represents the notification dto factory.
 */
@Slf4j
@Component
public class NotificationDTOFactory {

    private final FeedService feedService;
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
     * @param feedService                     {@link FeedService}.
     * @param feedDTOFactory                  {@link FeedDTOFactory}.
     * @param reactionDTOFactory              {@link ReactionDTOFactory}.
     * @param commentDTOFactory               {@link CommentDTOFactory}.
     * @param userService                     {@link UserService}.
     * @param userDTOFactory                  {@link UserDTOFactory}.
     * @param islandService                   {@link IslandService}.
     * @param islandDTOFactory                {@link IslandDTOFactory}.
     * @param systemNotificationConfiguration {@link SystemNotificationConfiguration}.
     */
    public NotificationDTOFactory(FeedService feedService,
                                  FeedDTOFactory feedDTOFactory,
                                  ReactionDTOFactory reactionDTOFactory,
                                  CommentDTOFactory commentDTOFactory,
                                  UserService userService,
                                  UserDTOFactory userDTOFactory,
                                  IslandService islandService,
                                  IslandDTOFactory islandDTOFactory,
                                  SystemNotificationConfiguration systemNotificationConfiguration) {
        this.feedService = feedService;
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
        countDTO.setUnreadReactionsCount(unreadNotificationsCountMessage.getUnreadReactionsCount());
        countDTO.setUnreadIslandNoticesCount(unreadNotificationsCountMessage.getUnreadIslandNoticesCount());

        countDTO.setHasUnread(countDTO.getUnreadCommentsCount() > 0
                || countDTO.getUnreadIslandNoticesCount() > 0
                || countDTO.getUnreadReactionsCount() > 0);

        return countDTO;
    }

    /**
     * Converts {@link UnreadNotificationsCountMessage} to {@link UnreadNotificationCountDTOV2}.
     *
     * @param unreadNotificationsCountMessage {@link UnreadNotificationsCountMessage}.
     * @return {@link UnreadNotificationCountDTOV2}.
     */
    public UnreadNotificationCountDTOV2 valueOfV2(UnreadNotificationsCountMessage unreadNotificationsCountMessage) {
        if (Objects.isNull(unreadNotificationsCountMessage)) {
            return null;
        }

        UnreadNotificationCountDTOV2 countDTO = new UnreadNotificationCountDTOV2();
        countDTO.setUnreadCommentsCount(unreadNotificationsCountMessage.getUnreadCommentsCount());
        countDTO.setUnreadReactionsCount(unreadNotificationsCountMessage.getUnreadReactionsCount());

        UnreadIslandNoticesCountDTO islandNoticesCountDTO = new UnreadIslandNoticesCountDTO();
        islandNoticesCountDTO.setUnreadNewMemberNoticeCount(unreadNotificationsCountMessage.getUnreadNewMembersCount());
        islandNoticesCountDTO.setUnreadNewSubscriberNoticeCount(unreadNotificationsCountMessage.getUnreadNewSubscribersCount());

        countDTO.setUnreadIslandNoticesCountDTO(islandNoticesCountDTO);

        UnreadQuestsionNoticesCountDTO unreadQuestsionNoticesCountDTO = new UnreadQuestsionNoticesCountDTO();
        unreadQuestsionNoticesCountDTO.setUnreadQuestionsNoticeCount(unreadNotificationsCountMessage.getUnreadNewQuestionCount());
        unreadQuestsionNoticesCountDTO.setUnreadAnswersNoticeCount(unreadNotificationsCountMessage.getUnreadNewAnswerCount());

        countDTO.setUnreadBoxesCount(unreadQuestsionNoticesCountDTO);
        countDTO.setHasUnread(countDTO.getUnreadCommentsCount() > 0
                || unreadNotificationsCountMessage.getUnreadIslandNoticesCount() > 0
                || countDTO.getUnreadReactionsCount() > 0
                || unreadNotificationsCountMessage.getUnreadNewQuestionCount() > 0
                || unreadNotificationsCountMessage.getUnreadNewAnswerCount() > 0);

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
                            .setFeedService(this.feedService)
                            .setCommentDTOFactory(this.commentDTOFactory)
                            .build();
                case NOTIFICATION_REACTIONS:
                    return new ReactionNotificationDTOBuilder()
                            .setNotificationMessage(notification)
                            .setFeedDTOFactory(this.feedDTOFactory)
                            .setReactionDTOFactory(this.reactionDTOFactory)
                            .build();
                case NOTIFICATION_BOX_NOTICE:
                    return new QuestionBoxNotificationDTOBuilder()
                            .setFeedService(this.feedService)
                            .setNotificationMessage(notification)
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
