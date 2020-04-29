package com.keepreal.madagascar.tenrecs.factory.notificationMessage;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import com.keepreal.madagascar.tenrecs.model.Comment;
import com.keepreal.madagascar.tenrecs.model.Feed;
import com.keepreal.madagascar.tenrecs.model.Notification;
import org.springframework.util.StringUtils;

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
     * Sets the notificaton.
     *
     * @param notification {@link Notification}.
     * @return this.
     */
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
                || !notification.getType().equals(NotificationType.NOTIFICATION_COMMENTS)) {
            return null;
        }

        return NotificationMessage.newBuilder()
                .setId(String.valueOf(this.notification.getId()))
                .setType(NotificationType.NOTIFICATION_COMMENTS)
                .setUserId(this.notification.getUserId())
                .setHasRead(this.notification.getCreatedAt().compareTo(this.lastReadTimestamp) < 0)
                .setFeed(this.toFeedMessage(this.notification.getFeed()))
                .setComment(this.toCommentMessage(this.notification.getComment()))
                .setCreatedAt(this.notification.getCreatedAt())
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

        return FeedMessage.newBuilder()
                .setId(feed.getId())
                .setUserId(feed.getAuthorId())
                .setText(feed.getText())
                .setIslandId(feed.getIslandId())
                .addAllImageUris(feed.getImageUris())
                .setFromHost(feed.getFromHost())
                .setCreatedAt(feed.getCreatedAt())
                .setCommentsCount(0)
                .setLikesCount(0)
                .setRepostCount(0)
                .build();
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

        CommentMessage.Builder commentBuilder = CommentMessage.newBuilder()
                .setId(comment.getId())
                .setFeedId(comment.getFeedId())
                .setContent(comment.getContent())
                .setUserId(comment.getAuthorId())
                .setCreatedAt(comment.getCreatedAt());

        if (StringUtils.isEmpty(comment.getReplyToId())) {
            commentBuilder.setReplyToId(StringValue.of(comment.getReplyToId()));
        }

        return commentBuilder.build();
    }

}
