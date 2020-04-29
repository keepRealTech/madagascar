package com.keepreal.madagascar.lemur.dtoFactory.notificationBuilder;

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
        notificationDTO.setCreatedAt(this.notificationMessage.getCreatedAt());

        if (Objects.nonNull(this.notificationMessage.getCommentNotification())) {
            notificationDTO.setFeed(
                    this.feedDTOFactory.briefValueOf(this.notificationMessage.getCommentNotification().getFeed()));
            notificationDTO.setComment(
                    this.commentDTOFactory.valueOf(this.notificationMessage.getCommentNotification().getComment()));
        }

        return notificationDTO;
    }

}
