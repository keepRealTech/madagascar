package com.keepreal.madagascar.hoopoe.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.IslandsResponse;
import com.keepreal.madagascar.coua.QueryIslandCondition;
import com.keepreal.madagascar.coua.RetrieveMultipleIslandsRequest;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class IslandService {

    private final Channel channel;

    public IslandService(@Qualifier("couaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * 根据hostId 获取创建的岛
     *
     * @param hostId    host id
     * @return          {@link List<IslandMessage>}
     */
    public List<IslandMessage> retrieveIslandsByHostId(String hostId) {

        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        QueryIslandCondition.Builder conditionBuilder = QueryIslandCondition.newBuilder();

        if (!StringUtils.isEmpty(hostId)) {
            conditionBuilder.setHostId(StringValue.of(hostId));
        }

        RetrieveMultipleIslandsRequest request = RetrieveMultipleIslandsRequest.newBuilder()
                .setCondition(conditionBuilder.build())
                .setPageRequest(PageRequest.newBuilder().setPage(0).setPageSize(10).build())
                .build();

        IslandsResponse islandsResponse;
        try {
            islandsResponse = stub.retrieveIslandsByCondition(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(islandsResponse)
                || !islandsResponse.hasStatus()) {
            log.error(Objects.isNull(islandsResponse) ? "Retrieve islands returned null." : islandsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != islandsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(islandsResponse.getStatus());
        }

        return islandsResponse.getIslandsList();
    }

}
