package com.keepreal.madagascar.lemur.dtoFactory.notificationBuilder;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.lemur.dtoFactory.CommentDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.FeedDTOFactory;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import swagger.model.NotificationDTO;
import swagger.model.NotificationType;

import java.util.Objects;

/**
 * Represents the comment notification dto builder.
 */
public class CommentNotificationDTOBuilder implements NotificationDTOBuilder {

    private FeedMessage feedMessage;
    private boolean islandSubscribed = true;
    private boolean isDeleted = false;
    private NotificationMessage notificationMessage;
    private CommentDTOFactory commentDTOFactory;
    private FeedDTOFactory feedDTOFactory;

    /**
     * Sets the {@link CommentDTOFactory}.
     *
     * @param commentDTOFactory {@link CommentDTOFactory}.
     * @return {@link CommentNotificationDTOBuilder}.
     */
    public CommentNotificationDTOBuilder setCommentDTOFactory(CommentDTOFactory commentDTOFactory) {
        this.commentDTOFactory = commentDTOFactory;
        return this;
    }

    /**
     * Sets the {@link FeedMessage}.
     *
     * @param islandSubscribed Whether island subscribed.
     * @return {@link NoticeNotificationDTOBuilder}.
     */
    public CommentNotificationDTOBuilder setIslandSubscribed(boolean islandSubscribed) {
        this.islandSubscribed = islandSubscribed;
        return this;
    }

    /**
     * Sets the {@link FeedMessage}.
     *
     * @param isDeleted Whether comment deleted.
     * @return {@link NoticeNotificationDTOBuilder}.
     */
    public CommentNotificationDTOBuilder setCommentDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
        return this;
    }

    /**
     * Sets the {@link FeedMessage}.
     *
     * @param feedMessage {@link FeedMessage}.
     * @return {@link NoticeNotificationDTOBuilder}.
     */
    public CommentNotificationDTOBuilder setFeedMessage(FeedMessage feedMessage) {
        this.feedMessage = feedMessage;
        return this;
    }

    /**
     * Sets the {@link FeedDTOFactory}.
     *
     * @param feedDTOFactory {@link FeedDTOFactory}.
     * @return {@link CommentNotificationDTOBuilder}.
     */
    public CommentNotificationDTOBuilder setFeedDTOFactory(FeedDTOFactory feedDTOFactory) {
        this.feedDTOFactory = feedDTOFactory;
        return this;
    }

    /**
     * Sets the notification message.
     *
     * @param notificationMessage {@link NotificationMessage}.
     * @return {@link CommentNotificationDTOBuilder}.
     */
    @Override
    public CommentNotificationDTOBuilder setNotificationMessage(NotificationMessage notificationMessage) {
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
                || Objects.isNull(this.commentDTOFactory)) {
            return null;
        }

        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setId(this.notificationMessage.getId());
        notificationDTO.setHasRead(this.notificationMessage.getHasRead());
        notificationDTO.setNotificationType(NotificationType.COMMENTS);
        notificationDTO.setCreatedAt(this.notificationMessage.getTimestamp());

        if (Objects.nonNull(this.notificationMessage.getCommentNotification())) {
            notificationDTO.setFeed(
                    this.feedDTOFactory.snapshotValueOf(this.notificationMessage.getCommentNotification().getFeed(), this.islandSubscribed, Objects.isNull(this.feedMessage)));
            notificationDTO.setComment(
                    this.commentDTOFactory.valueOfWithDeleted(this.notificationMessage.getCommentNotification().getComment(), this.isDeleted));
        }

        return notificationDTO;
    }

}
