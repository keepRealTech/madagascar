package com.keepreal.madagascar.vanga.service;

import com.google.protobuf.StringValue;
import com.google.protobuf.UInt64Value;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.CreateOrUpdateSupportTargetRequest;
import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.RetrieveSupportTargetsRequest;
import com.keepreal.madagascar.coua.SupportTargetMessage;
import com.keepreal.madagascar.coua.SupportTargetResponse;
import com.keepreal.madagascar.coua.SupportTargetsResponse;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * Represents the support target service.
 */
@Slf4j
@Service
public class SupportTargetService {

    private final Channel channel;

    public SupportTargetService(@Qualifier("couaChannel") Channel channel) {
        this.channel = channel;
    }

    @Transactional
    public void UpdateSupportTargetIfExisted(String hostId, Long amountInCents) {
        List<SupportTargetMessage> messages = this.retrieveSupportTargetByHostId(hostId);
        if (!CollectionUtils.isEmpty(messages)) {
            SupportTargetMessage supportTargetMessage = messages.get(0);
            this.updateSupportTarget(supportTargetMessage, amountInCents);
        }
    }

    /**
     * 根据岛主id 获取支持目标
     *
     * @param hostId 岛主id
     * @return {@link List<SupportTargetMessage>}
     */
    private List<SupportTargetMessage> retrieveSupportTargetByHostId(String hostId) {
        RetrieveSupportTargetsRequest request = RetrieveSupportTargetsRequest.newBuilder().setHostId(hostId).build();

        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        SupportTargetsResponse response;
        try {
            response = stub.retrieveSupportTargets(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "retrieve support targets returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getSupportTargetsList();
    }

    /**
     * 更新支持目标
     * @param supportTargetMessage {@link SupportTargetMessage}
     * @param amountInCents 增加金额
     */
    private void updateSupportTarget(SupportTargetMessage supportTargetMessage, Long amountInCents) {

        CreateOrUpdateSupportTargetRequest.Builder builder = CreateOrUpdateSupportTargetRequest.newBuilder()
                .setId(StringValue.of(supportTargetMessage.getId()));

        switch (supportTargetMessage.getTargetType()) {
            case SUPPORTER:
                builder.setTotalSupporterNum(UInt64Value.of(supportTargetMessage.getTotalSupporterNum() + 1L));
                break;
            case AMOUNT:
                builder.setTotalAmountInCents(UInt64Value.of(supportTargetMessage.getTotalAmountInCents() + amountInCents));
                break;
            case UNRECOGNIZED:
                log.error("unknown support target type target id is {}", supportTargetMessage.getId());
                break;
        }

        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        SupportTargetResponse response;
        try {
            response = stub.createOrUpdateSupportTarget(builder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "update support targets returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }
    }

}
