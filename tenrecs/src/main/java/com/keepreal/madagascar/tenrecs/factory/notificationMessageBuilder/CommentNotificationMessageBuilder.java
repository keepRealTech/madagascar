package com.keepreal.madagascar.tenrecs.factory.notificationMessageBuilder;

import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.MediaType;
import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.tenrecs.CommentNotificationMessage;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import com.keepreal.madagascar.tenrecs.model.Comment;
import com.keepreal.madagascar.tenrecs.model.Feed;
import com.keepreal.madagascar.tenrecs.model.Notification;
import com.keepreal.madagascar.tenrecs.util.MediaMessageConvertUtils;

import java.util.Objects;

/**
 * Implements the {@link NotificationMessageBuilder}.
 */
public class CommentNotificationMessageBuilder implements NotificationMessageBuilder {

    private long lastReadTimestamp;
    private Notification notification;

    /**
     * Sets the last read timestamp.
     *
     * @param lastReadTimestamp Last read comment notification timestamp.
     * @return this.
     */
    public CommentNotificationMessageBuilder setLastReadTimestamp(long lastReadTimestamp) {
        this.lastReadTimestamp = lastReadTimestamp;
        return this;
    }

    /**
     * Sets the notification.
     *
     * @param notification {@link Notification}.
     * @return this.
     */
    @Override
    public CommentNotificationMessageBuilder setNotification(Notification notification) {
        this.notification = notification;
        return this;
    }

    /**
     * Builds the {@link NotificationMessage}.
     *
     * @return {@link NotificationMessage}.
     */
    @Override
    public NotificationMessage build() {
        if (Objects.isNull(this.notification)
                || !this.notification.getType().equals(NotificationType.NOTIFICATION_COMMENTS)) {
            return null;
        }

        CommentNotificationMessage commentNotificationMessage = CommentNotificationMessage.newBuilder()
                .setFeed(this.toFeedMessage(this.notification.getFeed()))
                .setComment(this.toCommentMessage(this.notification.getComment()))
                .build();

        return NotificationMessage.newBuilder()
                .setId(this.notification.getId())
                .setType(NotificationType.NOTIFICATION_COMMENTS)
                .setUserId(this.notification.getUserId())
                .setHasRead(this.notification.getCreatedAt().compareTo(this.lastReadTimestamp) < 0)
                .setCommentNotification(commentNotificationMessage)
                .setTimestamp(this.notification.getTimestamp())
                .build();
    }

    /**
     * Converts {@link Feed} into {@link FeedMessage}.
     *
     * @param feed {@link Feed}.
     * @return {@link FeedMessage}.
     */
    private FeedMessage toFeedMessage(Feed feed) {
        if (Objects.isNull(feed)) {
            return null;
        }

        FeedMessage.Builder builder = FeedMessage.newBuilder()
                .setId(feed.getId())
                .setUserId(feed.getAuthorId())
                .setText(feed.getText())
                .setIslandId(feed.getIslandId())
                .addAllImageUris(feed.getImageUris())
                .setCreatedAt(feed.getCreatedAt())
                .setCommentsCount(0)
                .setLikesCount(0)
                .setRepostCount(0);

        MediaMessageConvertUtils.processMedia(builder, feed);
        return builder.build();
    }

    /**
     * Converts {@link Comment} into {@link CommentMessage}.
     *
     * @param comment {@link Comment}.
     * @return {@link CommentMessage}.
     */
    private CommentMessage toCommentMessage(Comment comment) {
        if (Objects.isNull(comment)) {
            return null;
        }

        return CommentMessage.newBuilder()
                .setId(comment.getId())
                .setFeedId(comment.getFeedId())
                .setContent(comment.getContent())
                .setUserId(comment.getAuthorId())
                .setReplyToId(comment.getReplyToId())
                .setCreatedAt(comment.getCreatedAt())
                .build();
    }

}
