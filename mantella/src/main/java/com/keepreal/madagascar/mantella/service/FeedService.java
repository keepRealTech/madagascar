package com.keepreal.madagascar.mantella.service;

import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.fossa.QueryFeedCondition;
import com.keepreal.madagascar.fossa.ReactorFeedServiceGrpc;
import com.keepreal.madagascar.fossa.RetrieveMultipleFeedsRequest;
import com.keepreal.madagascar.fossa.TimelineFeedMessage;
import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Represents the feed service.
 */
@Service
public class FeedService {

    private final Channel fossaChannel;

    /**
     * Constructs the island service.
     *
     * @param fossaChannel GRpc connection with service fossa.
     */
    public FeedService(@Qualifier("fossaChannel") Channel fossaChannel) {
        this.fossaChannel = fossaChannel;
    }

    /**
     * Retrieves feeds by island id and timestamp after.
     *
     * @param islandId       Island id.
     * @param timestampAfter Timestamp after.
     * @param pageSize       Page size.
     * @return {@link FeedMessage}.
     */
    public Flux<TimelineFeedMessage> retrieveFeedsByIslandIdAndTimestampBefore(String islandId, Long timestampAfter, Integer pageSize) {
        ReactorFeedServiceGrpc.ReactorFeedServiceStub stub = ReactorFeedServiceGrpc.newReactorStub(this.fossaChannel);

        QueryFeedCondition condition = QueryFeedCondition.newBuilder()
                .setIslandId(StringValue.of(islandId))
                .setTimestampBefore(Int64Value.of(timestampAfter))
                .build();

        RetrieveMultipleFeedsRequest request = RetrieveMultipleFeedsRequest.newBuilder()
                .setCondition(condition)
                .setPageRequest(PageRequest.newBuilder()
                        .setPage(0)
                        .setPageSize(pageSize)
                        .build())
                .build();

        return stub.retrieveMultipleTimelineFeeds(request)
                .flatMapMany(feedsResponse -> Flux.fromIterable(feedsResponse.getMessageList()));
    }

}
