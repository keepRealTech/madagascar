package com.keepreal.madagascar.mantella.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.IslandSubscribersResponse;
import com.keepreal.madagascar.coua.IslandsResponse;
import com.keepreal.madagascar.coua.QueryIslandCondition;
import com.keepreal.madagascar.coua.ReactorIslandServiceGrpc;
import com.keepreal.madagascar.coua.RetrieveIslandSubscribersByIdRequest;
import com.keepreal.madagascar.coua.RetrieveMultipleIslandsRequest;
import com.keepreal.madagascar.mantella.utils.PaginationUtils;
import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the island service.
 */
@Service
public class IslandService {

    private final Channel couaChannel;

    /**
     * Constructs the island service.
     *
     * @param couaChannel GRpc connection with service coua.
     */
    public IslandService(@Qualifier("couaChannel") Channel couaChannel) {
        this.couaChannel = couaChannel;
    }

    /**
     * Retrieves the island subscribers.
     *
     * @param islandId Island id.
     * @return Flux of user ids.
     */
    public Flux<String> retrieveSubscriberIdsByIslandId(String islandId) {
        ReactorIslandServiceGrpc.ReactorIslandServiceStub stub = ReactorIslandServiceGrpc.newReactorStub(this.couaChannel);

        RetrieveIslandSubscribersByIdRequest request = RetrieveIslandSubscribersByIdRequest.newBuilder()
                .setId(islandId)
                .setPageRequest(PaginationUtils.buildPageRequest(0, Integer.MAX_VALUE))
                .build();

        return stub.retrieveIslandSubscribersById(request)
                .filter(userResponse -> ErrorCode.REQUEST_SUCC_VALUE == (userResponse.getStatus().getRtn()))
                .flatMapIterable(IslandSubscribersResponse::getUserList)
                .map(UserMessage::getId);
    }

    public List<String> retrieveIslandIdListByUserId(String userId) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.couaChannel);

        IslandsResponse response = stub.retrieveIslandsByCondition(RetrieveMultipleIslandsRequest.newBuilder()
                .setCondition(QueryIslandCondition.newBuilder()
                        .setSubscribedUserId(StringValue.of(userId))
                        .build())
                .setPageRequest(PageRequest.newBuilder()
                        .setPageSize(100)
                        .build())
                .build());

        return response.getIslandsList().stream().map(IslandMessage::getId).collect(Collectors.toList());
    }
}
