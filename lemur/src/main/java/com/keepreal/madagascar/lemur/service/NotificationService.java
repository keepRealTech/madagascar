package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.common.NotificationTypeValue;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import com.keepreal.madagascar.tenrecs.CountUnreadNotificationsRequest;
import com.keepreal.madagascar.tenrecs.CountUnreadNotificationsResponse;
import com.keepreal.madagascar.tenrecs.NotificationServiceGrpc;
import com.keepreal.madagascar.tenrecs.NotificationsResponse;
import com.keepreal.madagascar.tenrecs.QueryNotificationCondition;
import com.keepreal.madagascar.tenrecs.RetrieveMultipleNotificationsRequest;
import com.keepreal.madagascar.tenrecs.UnreadNotificationsCountMessage;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the notification service.
 */
@Service
@Slf4j
public class NotificationService {

    private final Channel channel;

    /**
     * Constructs the notification service.
     *
     * @param channel GRpc managed channel connection to service Tenrecs.
     */
    public NotificationService(@Qualifier("tenrecsChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Retrieves notifications.
     *
     * @param userId   User id.
     * @param type     {@link NotificationType}.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link NotificationsResponse}.
     */
    public NotificationsResponse retrieveNotifications(String userId, NotificationType type, int page, int pageSize) {
        NotificationServiceGrpc.NotificationServiceBlockingStub stub = NotificationServiceGrpc.newBlockingStub(this.channel);

        QueryNotificationCondition.Builder conditionBuilder = QueryNotificationCondition.newBuilder()
                .setUserId(StringValue.of(userId));

        if (Objects.nonNull(type)) {
            conditionBuilder.setType(NotificationTypeValue.newBuilder().setValue(type).build());
        }

        RetrieveMultipleNotificationsRequest request = RetrieveMultipleNotificationsRequest.newBuilder()
                .setCondition(conditionBuilder.build())
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        NotificationsResponse notificationsResponse;
        try {
            notificationsResponse = stub.retrieveMultipleNotifications(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(notificationsResponse)
                || !notificationsResponse.hasStatus()) {
            log.error(Objects.isNull(notificationsResponse) ? "Count notification returned null." : notificationsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != notificationsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(notificationsResponse.getStatus());
        }

        return notificationsResponse;
    }

    /**
     * Retrieves the unread notifications count.
     *
     * @param userId User id.
     * @return {@link UnreadNotificationsCountMessage}.
     */
    public UnreadNotificationsCountMessage countUnreadNotifications(String userId) {
        NotificationServiceGrpc.NotificationServiceBlockingStub stub = NotificationServiceGrpc.newBlockingStub(this.channel);

        CountUnreadNotificationsRequest request = CountUnreadNotificationsRequest.newBuilder()
                .setUserId(userId)
                .build();

        CountUnreadNotificationsResponse countUnreadNotificationsResponse;
        try {
            countUnreadNotificationsResponse = stub.countUnreadNotifications(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(countUnreadNotificationsResponse)
                || !countUnreadNotificationsResponse.hasStatus()) {
            log.error(Objects.isNull(countUnreadNotificationsResponse) ? "Count notification returned null." : countUnreadNotificationsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != countUnreadNotificationsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(countUnreadNotificationsResponse.getStatus());
        }

        return countUnreadNotificationsResponse.getUnreadCounts();
    }

}
