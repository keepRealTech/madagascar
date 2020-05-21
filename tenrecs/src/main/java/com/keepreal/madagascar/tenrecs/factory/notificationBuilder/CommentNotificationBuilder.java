package com.keepreal.madagascar.tenrecs.factory.notificationBuilder;

import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import com.keepreal.madagascar.tenrecs.model.Comment;
import com.keepreal.madagascar.tenrecs.model.Feed;
import com.keepreal.madagascar.tenrecs.model.Notification;

import java.util.Objects;

/**
 * Implements the {@link NotificationBuilder}.
 */
public class CommentNotificationBuilder implements NotificationBuilder {

    private NotificationEvent event;

    /**
     * Sets the notification event.
     *
     * @param event {@link NotificationEvent}.
     * @return this.
     */
    @Override
    public CommentNotificationBuilder setEvent(NotificationEvent event) {
        this.event = event;
        return this;
    }

    /**
     * Builds the {@link Notification}.
     *
     * @return {@link Notification}.
     */
    @Override
    public Notification build() {
        if (Objects.isNull(this.event)
                || !this.event.getType().equals(NotificationEventType.NOTIFICATION_EVENT_NEW_COMMENT)
                || Objects.isNull(this.event.getCommentEvent())) {
            return null;
        }

        if (Objects.equals(this.event.getCommentEvent().getComment().getReplyToId(),
                this.event.getCommentEvent().getComment().getUserId())
            || Objects.equals(this.event.getCommentEvent().getFeed().getUserId(),
                this.event.getUserId())) {
            return null;
        }

        return Notification.builder()
                .type(NotificationType.NOTIFICATION_COMMENTS)
                .userId(this.event.getUserId())
                .eventId(this.event.getEventId())
                .timestamp(this.event.getTimestamp())
                .feed(this.toFeed(this.event.getCommentEvent().getFeed()))
                .comment(this.toComment(this.event.getCommentEvent().getComment()))
                .build();
    }

    /**
     * Converts {@link FeedMessage} into {@link Feed}.
     *
     * @param feedMessage {@link FeedMessage}.
     * @return {@link Feed}.
     */
    private Feed toFeed(FeedMessage feedMessage) {
        if (Objects.isNull(feedMessage)) {
            return null;
        }

        return Feed.builder()
                .id(feedMessage.getId())
                .islandId(feedMessage.getIslandId())
                .authorId(feedMessage.getUserId())
                .text(feedMessage.getText())
                .imageUris(feedMessage.getImageUrisList())
                .createdAt(feedMessage.getCreatedAt())
                .build();
    }

    /**
     * Converts {@link CommentMessage} into {@link Comment}.
     *
     * @param commentMessage {@link CommentMessage}.
     * @return {@link Comment}.
     */
    private Comment toComment(CommentMessage commentMessage) {
        if (Objects.isNull(commentMessage)) {
            return null;
        }

        return Comment.builder()
                .id(commentMessage.getId())
                .authorId(commentMessage.getUserId())
                .feedId(commentMessage.getFeedId())
                .content(commentMessage.getContent())
                .replyToId(commentMessage.getReplyToId())
                .createdAt(commentMessage.getCreatedAt())
                .build();
    }

}