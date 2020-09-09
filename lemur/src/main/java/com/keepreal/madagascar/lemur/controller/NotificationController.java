package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.NoticeType;
import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.dtoFactory.NotificationDTOFactory;
import com.keepreal.madagascar.lemur.service.CommentService;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.NotificationService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import com.keepreal.madagascar.tenrecs.UnreadNotificationsCountMessage;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.NotificationApi;
import swagger.model.NotificationsCountResponse;
import swagger.model.NotificationsCountResponseV2;
import swagger.model.NotificationsResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the notification controller.
 */
@RestController
public class NotificationController implements NotificationApi {

    private final IslandService islandService;
    private final FeedService feedService;
    private final CommentService commentService;
    private final NotificationService notificationService;
    private final NotificationDTOFactory notificationDTOFactory;

    /**
     * Constructs the notification controller.
     *
     * @param islandService          {@link IslandService}.
     * @param feedService            {@link FeedService}.
     * @param commentService         {@link CommentService}.
     * @param notificationService    {@link NotificationService}.
     * @param notificationDTOFactory {@link NotificationDTOFactory}.
     */
    public NotificationController(IslandService islandService,
                                  FeedService feedService,
                                  CommentService commentService,
                                  NotificationService notificationService,
                                  NotificationDTOFactory notificationDTOFactory) {
        this.islandService = islandService;
        this.feedService = feedService;
        this.commentService = commentService;
        this.notificationService = notificationService;
        this.notificationDTOFactory = notificationDTOFactory;
    }

    /**
     * Implements the notification count api.
     *
     * @return {@link NotificationsCountResponse}.
     */
    @Override
    public ResponseEntity<NotificationsCountResponse> apiV1NotificationsCountGet() {
        String userId = HttpContextUtils.getUserIdFromContext();

        UnreadNotificationsCountMessage unreadNotificationsCountMessage = this.notificationService.countUnreadNotifications(userId);

        NotificationsCountResponse response = new NotificationsCountResponse();
        response.setData(this.notificationDTOFactory.valueOf(unreadNotificationsCountMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the notification count api.
     *
     * @return {@link NotificationsCountResponseV2}.
     */
    @Override
    public ResponseEntity<NotificationsCountResponseV2> apiV11NotificationsCountGet() {
        String userId = HttpContextUtils.getUserIdFromContext();

        UnreadNotificationsCountMessage unreadNotificationsCountMessage = this.notificationService.countUnreadNotifications(userId);

        NotificationsCountResponseV2 response = new NotificationsCountResponseV2();
        response.setData(this.notificationDTOFactory.valueOfV2(unreadNotificationsCountMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the notification get api.
     *
     * @param type     {@link NotificationType}.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link NotificationsResponse}.
     */
    @Cacheable(value = "default-system-notice-response", condition = "T(swagger.model.NotificationType).SYSTEM_NOTICE.equals(#type)")
    @Override
    public ResponseEntity<NotificationsResponse> apiV1NotificationsGet(swagger.model.NotificationType type,
                                                                       swagger.model.NoticeType noticeType,
                                                                       Integer page,
                                                                       Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();

        com.keepreal.madagascar.tenrecs.NotificationsResponse notificationsResponse;
        if (swagger.model.NotificationType.SYSTEM_NOTICE.equals(type)) {
            notificationsResponse = com.keepreal.madagascar.tenrecs.NotificationsResponse.newBuilder()
                    .addNotifications(this.defaultSystemNotificationMessage())
                    .build();
        } else {
            notificationsResponse =
                    this.notificationService.retrieveNotifications(userId, this.convertType(type),
                            this.convertNoticeType(noticeType), page, pageSize);
        }

        Set<String> feedIdSet = notificationsResponse.getNotificationsList()
                .stream()
                .map(this::collectFeedId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, FeedMessage> feedMap = new HashMap<>();

        if (!feedIdSet.isEmpty()) {
            feedMap = this.feedService.retrieveFeedsByIds(feedIdSet, userId)
                    .getFeedList()
                    .stream()
                    .collect(Collectors.toMap(FeedMessage::getId, Function.identity(), (feed1, feed2) -> feed1, HashMap::new));
        }

        Set<String> islandIdSet = notificationsResponse.getNotificationsList()
                .stream()
                .map(this::collectIslandId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Boolean> subscriptionMap = new HashMap<>();
        if (!islandIdSet.isEmpty()) {
            subscriptionMap = this.islandService.retrieveIslandSubscribeStateByUserId(userId, islandIdSet);
        }

        Set<String> commentIdSet = notificationsResponse.getNotificationsList()
                .stream()
                .map(this::collectCommentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, CommentMessage> commentMap = new HashMap<>();
        if (!islandIdSet.isEmpty()) {
            commentMap = this.commentService.retrieveCommentByIds(commentIdSet)
                    .stream()
                    .collect(Collectors.toMap(CommentMessage::getId, Function.identity(), (comment1, comment2) -> comment1, HashMap::new));
        }

        NotificationsResponse response = new NotificationsResponse();
        Map<String, FeedMessage> finalFeedMap = feedMap;
        Map<String, Boolean> finalSubscriptionMap = subscriptionMap;
        Map<String, CommentMessage> finalCommentMap = commentMap;
        response.setData(notificationsResponse.getNotificationsList()
                .stream()
                .map(notificationMessage -> this.notificationDTOFactory.valueOf(notificationMessage, finalFeedMap, finalSubscriptionMap, finalCommentMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(notificationsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Converts {@link swagger.model.NotificationType} to {@link NotificationType}.
     *
     * @param type {@link swagger.model.NotificationType}.
     * @return {@link NotificationType}.
     */
    private NotificationType convertType(swagger.model.NotificationType type) {
        if (Objects.isNull(type)) {
            return null;
        }

        switch (type) {
            case SYSTEM_NOTICE:
                return NotificationType.NOTIFICATION_SYSTEM_NOTICE;
            case COMMENTS:
                return NotificationType.NOTIFICATION_COMMENTS;
            case REACTIONS:
                return NotificationType.NOTIFICATION_REACTIONS;
            case ISLAND_NOTICE:
                return NotificationType.NOTIFICATION_ISLAND_NOTICE;
            case BOX_NOTICE:
                return NotificationType.NOTIFICATION_BOX_NOTICE;
            default:
                return NotificationType.UNRECOGNIZED;
        }
    }

    /**
     * Converts {@link swagger.model.NoticeType} to {@link NoticeType}.
     *
     * @param noticeType {@link swagger.model.NoticeType}.
     * @return {@link NoticeType}.
     */
    private NoticeType convertNoticeType(swagger.model.NoticeType noticeType) {
        if (Objects.isNull(noticeType)) {
            return null;
        }

        switch (noticeType) {
            case ISLAND_NOTICE_NEW_SUBSCRIBER:
                return NoticeType.NOTICE_TYPE_ISLAND_NEW_SUBSCRIBER;
            case ISLAND_NOTICE_NEW_MEMBER:
                return NoticeType.NOTICE_TYPE_ISLAND_NEW_MEMBER;
            case BOX_NOTICE_NEW_QUESTION:
                return NoticeType.NOTICE_TYPE_BOX_NEW_QUESTION;
            case BOX_NOTICE_NEW_ANSWER:
                return NoticeType.NOTICE_TYPE_BOX_NEW_ANSWER;
            default:
                return NoticeType.UNRECOGNIZED;
        }
    }

    /**
     * Constructs a default notification message entity.
     *
     * @return {@link NotificationMessage}.
     */
    private NotificationMessage defaultSystemNotificationMessage() {
        return NotificationMessage.newBuilder()
                .setType(NotificationType.NOTIFICATION_SYSTEM_NOTICE)
                .build();
    }

    /**
     * Extracts the feed id from notification message.
     *
     * @param notification {@link NotificationMessage}.
     * @return Feed id.
     */
    private String collectFeedId(NotificationMessage notification) {
        if (Objects.isNull(notification)) {
            return null;
        }

        switch (notification.getType()) {
            case NOTIFICATION_COMMENTS:
                return notification.getCommentNotification().getFeed().getId();
            case NOTIFICATION_REACTIONS:
                return notification.getReactionNotification().getFeed().getId();
            case NOTIFICATION_BOX_NOTICE:
                switch (notification.getNoticeNotification().getType()) {
                    case NOTICE_TYPE_BOX_NEW_QUESTION:
                        return notification.getNoticeNotification().getNewQuestionNotice().getFeedId();
                    case NOTICE_TYPE_BOX_NEW_ANSWER:
                        return notification.getNoticeNotification().getNewAnswerNotice().getFeedId();
                    default:
                        return null;
                }
            default:
                return null;
        }
    }

    /**
     * Extracts the island id from notification message.
     *
     * @param notification {@link NotificationMessage}.
     * @return Island id.
     */
    private String collectIslandId(NotificationMessage notification) {
        if (Objects.isNull(notification)) {
            return null;
        }

        switch (notification.getType()) {
            case NOTIFICATION_COMMENTS:
                return notification.getCommentNotification().getFeed().getIslandId();
            case NOTIFICATION_REACTIONS:
                return notification.getReactionNotification().getFeed().getIslandId();
            default:
                return null;
        }
    }

    /**
     * Extracts the comment id from notification message.
     *
     * @param notification {@link NotificationMessage}.
     * @return Island id.
     */
    private String collectCommentId(NotificationMessage notification) {
        if (Objects.isNull(notification)) {
            return null;
        }

        switch (notification.getType()) {
            case NOTIFICATION_COMMENTS:
                return notification.getCommentNotification().getComment().getId();
            default:
                return null;
        }
    }

}
