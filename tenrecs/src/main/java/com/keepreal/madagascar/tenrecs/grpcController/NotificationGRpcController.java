package com.keepreal.madagascar.tenrecs.grpcController;

import com.keepreal.madagascar.common.NoticeType;
import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.tenrecs.CountUnreadNotificationsRequest;
import com.keepreal.madagascar.tenrecs.CountUnreadNotificationsResponse;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import com.keepreal.madagascar.tenrecs.NotificationServiceGrpc;
import com.keepreal.madagascar.tenrecs.NotificationsResponse;
import com.keepreal.madagascar.tenrecs.RetrieveMultipleNotificationsRequest;
import com.keepreal.madagascar.tenrecs.UnreadNotificationsCountMessage;
import com.keepreal.madagascar.tenrecs.factory.NotificationMessageFactory;
import com.keepreal.madagascar.tenrecs.model.Notification;
import com.keepreal.madagascar.tenrecs.model.UserNotificationRecord;
import com.keepreal.madagascar.tenrecs.service.NotificationService;
import com.keepreal.madagascar.tenrecs.service.UserNotificationRecordService;
import com.keepreal.madagascar.tenrecs.util.CommonStatusUtils;
import com.keepreal.madagascar.tenrecs.util.PaginationUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the notification GRpc controller.
 */
@GRpcService
public class NotificationGRpcController extends NotificationServiceGrpc.NotificationServiceImplBase {

    private final NotificationService notificationService;
    private final UserNotificationRecordService userNotificationRecordService;
    private final NotificationMessageFactory notificationMessageFactory;

    /**
     * Constructs the notification grpc controller.
     *
     * @param notificationService           {@link NotificationService}.
     * @param userNotificationRecordService {@link UserNotificationRecordService}.
     * @param notificationMessageFactory    {@link NotificationMessage}.
     */
    public NotificationGRpcController(NotificationService notificationService,
                                      UserNotificationRecordService userNotificationRecordService,
                                      NotificationMessageFactory notificationMessageFactory) {
        this.notificationService = notificationService;
        this.userNotificationRecordService = userNotificationRecordService;
        this.notificationMessageFactory = notificationMessageFactory;
    }

    /**
     * Implements the get notifications method.
     *
     * @param request          {@link RetrieveMultipleNotificationsRequest}.
     * @param responseObserver {@link StreamObserver} Callback.
     */
    @Override
    public void retrieveMultipleNotifications(RetrieveMultipleNotificationsRequest request,
                                              StreamObserver<NotificationsResponse> responseObserver) {
        long timestamp = Instant.now().toEpochMilli();

        PageRequest pageRequest =
                request.hasPageRequest() ? request.getPageRequest() : PaginationUtils.defaultPageRequest();

        if (!request.hasCondition()
                || !request.getCondition().hasUserId()) {
            NotificationsResponse response = NotificationsResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_INVALID_ARGUMENT))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        UserNotificationRecord record =
                this.userNotificationRecordService.retrieveByUserId(request.getCondition().getUserId().getValue());

        String userId = request.getCondition().getUserId().getValue();
        NotificationType type = request.getCondition().getType().getValue();

        Page<Notification> notifications;
        if (!request.getCondition().hasType()) {
            notifications = this.notificationService.retrieveByUserIdWithPagination(userId, pageRequest);
        } else {
            if (NotificationType.NOTIFICATION_ISLAND_NOTICE.equals(request.getCondition().getType().getValue())
                    && request.getCondition().hasNoticeType()) {
                notifications = this.notificationService.retrieveByUserIdAndNoticeTypeWithPagination(userId, 
                        request.getCondition().getNoticeType().getValue(), pageRequest);
            } else if (NotificationType.NOTIFICATION_BOX_NOTICE.equals(request.getCondition().getType().getValue())
                    && request.getCondition().hasNoticeType()) {
                notifications = this.notificationService.retrieveByUserIdAndNoticeTypeWithPagination(userId,
                        request.getCondition().getNoticeType().getValue(), pageRequest);
            } else {
                notifications = this.notificationService.retrieveByUserIdAndTypeWithPagination(userId, type, pageRequest);
            }
        }

        NotificationsResponse response = NotificationsResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllNotifications(notifications.get()
                        .map(notification -> this.notificationMessageFactory.toNotificationMessage(notification, record))
                        .collect(Collectors.toList()))
                .setPageResponse(PaginationUtils.valueOf(notifications, pageRequest))
                .build();

        switch (type) {
            case NOTIFICATION_REACTIONS:
                record.setLastReadReactionNotificationTimestamp(timestamp);
                break;
            case NOTIFICATION_ISLAND_NOTICE:
                record.setLastReadIslandNoticeNotificationTimestamp(timestamp);
                if (request.getCondition().hasNoticeType()) {
                    switch (request.getCondition().getNoticeType().getValue()) {
                        case NOTICE_TYPE_ISLAND_NEW_MEMBER:
                            record.setLastReadIslandNoticeNewMemberNotificationTimestamp(timestamp);
                            break;
                        case NOTICE_TYPE_ISLAND_NEW_SUBSCRIBER:
                            record.setLastReadIslandNoticeNewSubscriberNotificationTimestamp(timestamp);
                            break;
                        default:
                    }
                } else {
                    record.setLastReadIslandNoticeNewMemberNotificationTimestamp(timestamp);
                    record.setLastReadIslandNoticeNewSubscriberNotificationTimestamp(timestamp);
                }
                break;
            case NOTIFICATION_BOX_NOTICE:
                record.setLastReadBoxNoticeNotificationTimestamp(timestamp);
                if (request.getCondition().hasNoticeType()) {
                    switch (request.getCondition().getNoticeType().getValue()) {
                        case NOTICE_TYPE_BOX_NEW_QUESTION:
                            record.setLastReadBoxNoticeNewQuestionNotificationTimestamp(timestamp);
                            break;
                        case NOTICE_TYPE_BOX_NEW_ANSWER:
                            record.setLastReadBoxNoticeNewReplyNotificationTimestamp(timestamp);
                            break;
                        default:
                    }
                } else {
                    record.setLastReadBoxNoticeNewQuestionNotificationTimestamp(timestamp);
                    record.setLastReadBoxNoticeNewReplyNotificationTimestamp(timestamp);
                }
                break;
            case NOTIFICATION_COMMENTS:
                record.setLastReadCommentNotificationTimestamp(timestamp);
                break;
            default:
                record.setLastReadReactionNotificationTimestamp(timestamp);
                record.setLastReadIslandNoticeNotificationTimestamp(timestamp);
                record.setLastReadCommentNotificationTimestamp(timestamp);
                record.setLastReadIslandNoticeNewSubscriberNotificationTimestamp(timestamp);
                record.setLastReadIslandNoticeNewMemberNotificationTimestamp(timestamp);
                record.setLastReadBoxNoticeNotificationTimestamp(timestamp);
                record.setLastReadBoxNoticeNewQuestionNotificationTimestamp(timestamp);
                record.setLastReadBoxNoticeNewReplyNotificationTimestamp(timestamp);
        }

        this.userNotificationRecordService.update(record);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the get unread notifications counts method.
     *
     * @param request          {@link CountUnreadNotificationsRequest}.
     * @param responseObserver {@link StreamObserver} Callback.
     */
    @Override
    public void countUnreadNotifications(CountUnreadNotificationsRequest request,
                                         StreamObserver<CountUnreadNotificationsResponse> responseObserver) {
        String userId = request.getUserId();
        UserNotificationRecord record = this.userNotificationRecordService.retrieveByUserId(userId);

        int newSubscriberCount = this.notificationService.countByUserIdAndNoticeTypeAndCreatedAtAfter(
                userId,
                NoticeType.NOTICE_TYPE_ISLAND_NEW_SUBSCRIBER,
                Objects.isNull(record.getLastReadIslandNoticeNewSubscriberNotificationTimestamp())
                        ? 0 : record.getLastReadIslandNoticeNewSubscriberNotificationTimestamp());

        int newMemberCount = this.notificationService.countByUserIdAndNoticeTypeAndCreatedAtAfter(
                userId,
                NoticeType.NOTICE_TYPE_ISLAND_NEW_MEMBER,
                Objects.isNull(record.getLastReadIslandNoticeNewMemberNotificationTimestamp())
                        ? 0 : record.getLastReadIslandNoticeNewMemberNotificationTimestamp());

        int newQuestionCount = this.notificationService.countByUserIdAndNoticeTypeAndCreatedAtAfter(
                userId,
                NoticeType.NOTICE_TYPE_BOX_NEW_QUESTION,
                Objects.isNull(record.getLastReadBoxNoticeNewQuestionNotificationTimestamp())
                        ? 0 : record.getLastReadBoxNoticeNewQuestionNotificationTimestamp());


        int newAnswerCount = this.notificationService.countByUserIdAndNoticeTypeAndCreatedAtAfter(
                userId,
                NoticeType.NOTICE_TYPE_BOX_NEW_ANSWER,
                Objects.isNull(record.getLastReadBoxNoticeNewReplyNotificationTimestamp())
                        ? 0 : record.getLastReadBoxNoticeNewReplyNotificationTimestamp());

        UnreadNotificationsCountMessage unreadNotificationsCountMessage =
                UnreadNotificationsCountMessage.newBuilder()
                        .setUnreadCommentsCount(
                                this.notificationService.countByUserIdAndTypeAndCreatedAtAfter(
                                        userId,
                                        NotificationType.NOTIFICATION_COMMENTS,
                                        record.getLastReadCommentNotificationTimestamp()))
                        .setUnreadReactionsCount(this.notificationService.countByUserIdAndTypeAndCreatedAtAfter(
                                userId,
                                NotificationType.NOTIFICATION_REACTIONS,
                                record.getLastReadReactionNotificationTimestamp()))
                        .setUnreadIslandNoticesCount(newMemberCount + newSubscriberCount)
                        .setUnreadNewSubscribersCount(newSubscriberCount)
                        .setUnreadNewMembersCount(newMemberCount)
                        .setUnreadNewQuestionCount(newQuestionCount)
                        .setUnreadNewAnswerCount(newAnswerCount)
                        .build();

        CountUnreadNotificationsResponse response = CountUnreadNotificationsResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setUnreadCounts(unreadNotificationsCountMessage)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
