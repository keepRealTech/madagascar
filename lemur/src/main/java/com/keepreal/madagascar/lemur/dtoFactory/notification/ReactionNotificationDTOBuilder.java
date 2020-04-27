package com.keepreal.madagascar.lemur.dtoFactory.notification;

import com.keepreal.madagascar.lemur.dtoFactory.FeedDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.ReactionDTOFactory;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import swagger.model.NotificationDTO;
import swagger.model.NotificationType;

import java.util.Objects;

/**
 * Represents the reaction notification dto builder.
 */
public class ReactionNotificationDTOBuilder implements NotificationDTOBuilder {

    private NotificationMessage notificationMessage;
    private ReactionDTOFactory reactionDTOFactory;
    private FeedDTOFactory feedDTOFactory;

    /**
     * Sets the {@link ReactionDTOFactory}.
     *
     * @param reactionDTOFactory {@link ReactionDTOFactory}.
     * @return {@link ReactionNotificationDTOBuilder}.
     */
    public ReactionNotificationDTOBuilder setReactionDTOFactory(ReactionDTOFactory reactionDTOFactory) {
        this.reactionDTOFactory = reactionDTOFactory;
        return this;
    }

    /**
     * Sets the {@link FeedDTOFactory}.
     *
     * @param feedDTOFactory {@link FeedDTOFactory}.
     * @return {@link ReactionNotificationDTOBuilder}.
     */
    public ReactionNotificationDTOBuilder setFeedDTOFactory(FeedDTOFactory feedDTOFactory) {
        this.feedDTOFactory = feedDTOFactory;
        return this;
    }

    /**
     * Sets the notification message.
     *
     * @param notificationMessage {@link NotificationMessage}.
     * @return {@link ReactionNotificationDTOBuilder}.
     */
    @Override
    public ReactionNotificationDTOBuilder setNotificationMessage(NotificationMessage notificationMessage) {
        this.notificationMessage = notificationMessage;
        return this;
    }

    /**
     * Builds the {@link NotificationDTO}.
     *
     * @return {@link NotificationDTO}.
     */
    @Override
    public NotificationDTO build() {
        if (Objects.isNull(this.notificationMessage)
                || Objects.isNull(this.feedDTOFactory)
                || Objects.isNull(this.reactionDTOFactory)) {
            return null;
        }

        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setId(this.notificationMessage.getId());
        notificationDTO.setHasRead(this.notificationMessage.getHasRead());
        notificationDTO.setNotificationType(NotificationType.REACTIONS);
        notificationDTO.setCreatedAt(this.notificationMessage.getCreatedAt());
        notificationDTO.setFeed(this.feedDTOFactory.briefValueOf(this.notificationMessage.getFeed()));
        notificationDTO.setReactions(this.reactionDTOFactory.valueOf(this.notificationMessage.getReaction()));

        return notificationDTO;
    }

}
