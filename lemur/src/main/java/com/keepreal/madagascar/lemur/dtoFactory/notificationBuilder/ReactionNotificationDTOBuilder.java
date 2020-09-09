package com.keepreal.madagascar.lemur.dtoFactory.notificationBuilder;

import com.keepreal.madagascar.common.FeedMessage;
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

    private FeedMessage feedMessage;
    private boolean islandSubscribed;
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
     * Sets the {@link FeedMessage}.
     *
     * @param islandSubscribed Whether island subscribed.
     * @return {@link NoticeNotificationDTOBuilder}.
     */
    public ReactionNotificationDTOBuilder setIslandSubscribed(boolean islandSubscribed) {
        this.islandSubscribed = islandSubscribed;
        return this;
    }

    /**
     * Sets the {@link FeedMessage}.
     *
     * @param feedMessage {@link FeedMessage}.
     * @return {@link NoticeNotificationDTOBuilder}.
     */
    public ReactionNotificationDTOBuilder setFeedMessage(FeedMessage feedMessage) {
        this.feedMessage = feedMessage;
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
        notificationDTO.setCreatedAt(this.notificationMessage.getTimestamp());

        if (Objects.nonNull(this.notificationMessage.getReactionNotification())) {
            notificationDTO.setFeed(
                    this.feedDTOFactory.snapshotValueOf(this.notificationMessage.getReactionNotification().getFeed(),
                            this.islandSubscribed,
                            Objects.isNull(this.feedMessage) || this.feedMessage.getIsAccess(),
                            Objects.isNull(this.feedMessage)));
            notificationDTO.setReactions(
                    this.reactionDTOFactory.valueOf(this.notificationMessage.getReactionNotification().getReaction()));
        }

        return notificationDTO;
    }

}
