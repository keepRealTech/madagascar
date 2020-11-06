package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.IncomeDetailType;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import com.keepreal.madagascar.vanga.IncomeMonthlyMessage;
import com.keepreal.madagascar.vanga.IncomeProfileMessage;
import com.keepreal.madagascar.vanga.IncomeServiceGrpc;
import com.keepreal.madagascar.vanga.RetrieveCurrentMonthResponse;
import com.keepreal.madagascar.vanga.RetrieveIncomeDetailRequest;
import com.keepreal.madagascar.vanga.RetrieveIncomeDetailResponse;
import com.keepreal.madagascar.vanga.RetrieveIncomeMonthlyResponse;
import com.keepreal.madagascar.vanga.RetrieveIncomeProfileResponse;
import com.keepreal.madagascar.vanga.RetrieveMyIncomeRequest;
import com.keepreal.madagascar.vanga.RetrieveSupportListRequest;
import com.keepreal.madagascar.vanga.RetrieveSupportListResponse;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import swagger.model.IncomeType;

import java.util.List;
import java.util.Objects;

/**
 * Represents the income info service.
 */
@Service
@Slf4j
public class IncomeService {

    private final Channel channel;

    /**
     * Constructs the income info service.
     *
     * @param channel GRpc managed channel connection to service Vanga.
     */
    public IncomeService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    public IncomeProfileMessage retrieveIncomeProfile(String userId) {
        IncomeServiceGrpc.IncomeServiceBlockingStub stub = IncomeServiceGrpc.newBlockingStub(this.channel);

        RetrieveMyIncomeRequest request = RetrieveMyIncomeRequest.newBuilder()
                .setUserId(userId)
                .build();

        RetrieveIncomeProfileResponse response;
        try {
            response = stub.retrieveIncomeProfile(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve income profile returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getMessage();
    }

    public RetrieveSupportListResponse retrieveSupportList(String userId, int page, int pageSize) {
        IncomeServiceGrpc.IncomeServiceBlockingStub stub = IncomeServiceGrpc.newBlockingStub(this.channel);

        RetrieveSupportListRequest request = RetrieveSupportListRequest.newBuilder()
                .setUserId(userId)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        RetrieveSupportListResponse response;
        try {
            response = stub.retrieveSupportList(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve support list returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    public RetrieveCurrentMonthResponse retrieveCurrentMonth(String userId) {
        IncomeServiceGrpc.IncomeServiceBlockingStub stub = IncomeServiceGrpc.newBlockingStub(this.channel);

        RetrieveMyIncomeRequest request = RetrieveMyIncomeRequest.newBuilder()
                .setUserId(userId)
                .build();

        RetrieveCurrentMonthResponse response;
        try {
            response = stub.retrieveCurrentMonth(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve current month returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    public List<IncomeMonthlyMessage> retrieveIncomeMonthly(String userId) {
        IncomeServiceGrpc.IncomeServiceBlockingStub stub = IncomeServiceGrpc.newBlockingStub(this.channel);

        RetrieveMyIncomeRequest request = RetrieveMyIncomeRequest.newBuilder()
                .setUserId(userId)
                .build();

        RetrieveIncomeMonthlyResponse response;
        try {
            response = stub.retrieveIncomeMonthly(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve support list returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getMessageList();
    }

    public RetrieveIncomeDetailResponse retrieveIncomeDetail(String userId,
                                                             IncomeDetailType type,
                                                             Long timestamp,
                                                             String membershipId,
                                                             int page,
                                                             int pageSize) {
        IncomeServiceGrpc.IncomeServiceBlockingStub stub = IncomeServiceGrpc.newBlockingStub(this.channel);

        RetrieveIncomeDetailRequest.Builder builder = RetrieveIncomeDetailRequest.newBuilder()
                .setUserId(userId)
                .setType(type)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize));

        if (Objects.nonNull(timestamp)) {
            builder.setTimestamp(Int64Value.of(timestamp));
        }

        if (Objects.nonNull(membershipId)) {
            builder.setMembershipId(StringValue.of(membershipId));
        }

        RetrieveIncomeDetailResponse response;
        try {
            response = stub.retrieveIncomeDetail(builder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve income detail returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }
}
