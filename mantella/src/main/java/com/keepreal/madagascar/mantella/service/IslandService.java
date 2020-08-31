package com.keepreal.madagascar.mantella.service;

import com.keepreal.madagascar.common.IslandAccessType;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.IslandResponse;
import com.keepreal.madagascar.coua.IslandSubscribersResponse;
import com.keepreal.madagascar.coua.ReactorIslandServiceGrpc;
import com.keepreal.madagascar.coua.RetrieveIslandByIdRequest;
import com.keepreal.madagascar.coua.RetrieveIslandSubscribersByIdRequest;
import com.keepreal.madagascar.mantella.utils.PaginationUtils;
import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    /**
     * Checks if the island is public access.
     *
     * @param islandId Island id.
     * @return True if it has public access.
     */
    public Mono<Boolean> checkIslandAccessTypeIsPublic(String islandId) {
        ReactorIslandServiceGrpc.ReactorIslandServiceStub stub = ReactorIslandServiceGrpc.newReactorStub(this.couaChannel);

        RetrieveIslandByIdRequest request = RetrieveIslandByIdRequest.newBuilder()
                .setId(islandId)
                .build();

        return stub.retrieveIslandById(request)
                .filter(islandResponse -> ErrorCode.REQUEST_SUCC_VALUE == (islandResponse.getStatus().getRtn()))
                .map(IslandResponse::getIsland)
                .map(islandMessage -> IslandAccessType.ISLAND_ACCESS_PUBLIC.equals(islandMessage.getIslandAccessType()));
    }

}
