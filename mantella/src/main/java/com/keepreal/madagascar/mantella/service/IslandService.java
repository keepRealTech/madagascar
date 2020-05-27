package com.keepreal.madagascar.mantella.service;

import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.IslandSubscribersResponse;
import com.keepreal.madagascar.coua.ReactorIslandServiceGrpc;
import com.keepreal.madagascar.coua.RetrieveIslandSubscribersByIdRequest;
import com.keepreal.madagascar.mantella.utils.PaginationUtils;
import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Represents the island service.
 */
@Service
public class IslandService {

    private final Channel couaChannel;

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

}
