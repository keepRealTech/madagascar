package com.keepreal.madagascar.tenrecs.grpcController;

import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
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
                || !request.getCondition().hasUserId()
                || !request.getCondition().hasType()) {
            NotificationsResponse response = NotificationsResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_INVALID_ARGUMENT))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        UserNotificationRecord record =
                this.userNotificationRecordService.retrieveByUserId(request.getCondition().getUserId().getValue());

        String userId = request.getCondition().getUserId().getValue();
        NotificationType type = request.getCondition().getType().getValue();

        Page<Notification> notifications =
                this.notificationService.retrieveByUserIdAndTypeWithPagination(userId, type, pageRequest);

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
                break;
            case NOTIFICATION_COMMENTS:
                record.setLastReadCommentNotificationTimestamp(timestamp);
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
                        .setUnreadIslandNoticesCount(this.notificationService.countByUserIdAndTypeAndCreatedAtAfter(
                                userId,
                                NotificationType.NOTIFICATION_ISLAND_NOTICE,
                                record.getLastReadIslandNoticeNotificationTimestamp()))
                        .build();

        CountUnreadNotificationsResponse response = CountUnreadNotificationsResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setUnreadCounts(unreadNotificationsCountMessage)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
