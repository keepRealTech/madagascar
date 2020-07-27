package com.keepreal.madagascar.mantella.gRpcController;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.mantella.ReactorTimelineServiceGrpc;
import com.keepreal.madagascar.mantella.RetrieveMultipleTimelinesRequest;
import com.keepreal.madagascar.mantella.TimelinesResponse;
import com.keepreal.madagascar.mantella.factory.TimelineMessageFactory;
import com.keepreal.madagascar.mantella.service.TimelineService;
import com.keepreal.madagascar.mantella.utils.CommonStatusUtils;
import org.lognet.springboot.grpc.GRpcService;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * Represents the timeline grpc controller.
 */
@GRpcService
public class TimelineGRpcController extends ReactorTimelineServiceGrpc.TimelineServiceImplBase {

    private final TimelineService timelineService;
    private final TimelineMessageFactory timelineMessageFactory;
    private final CommonStatusUtils commonStatusUtils;

    /**
     * Constructs the timeline grpc controller.
     *
     * @param timelineService        {@link TimelineService}.
     * @param timelineMessageFactory {@link TimelineMessageFactory}.
     * @param commonStatusUtils      {@link CommonStatusUtils}.
     */
    public TimelineGRpcController(TimelineService timelineService,
                                  TimelineMessageFactory timelineMessageFactory,
                                  CommonStatusUtils commonStatusUtils) {
        this.timelineService = timelineService;
        this.timelineMessageFactory = timelineMessageFactory;
        this.commonStatusUtils = commonStatusUtils;
    }

    /**
     * Implements the retrieve timelines logic.
     *
     * @param request {@link RetrieveMultipleTimelinesRequest}.
     * @return {@link TimelinesResponse}.
     */
    @Override
    public Mono<TimelinesResponse> retrieveMultipleTimelines(Mono<RetrieveMultipleTimelinesRequest> request) {
        return request.flatMapMany(retrieveMultipleTimelinesRequest ->
                retrieveMultipleTimelinesRequest.hasTimestampBefore() ?
                    this.timelineService.retrieveByUserIdAndCreatedTimestamp(
                            retrieveMultipleTimelinesRequest.getUserId(),
                            null,
                            retrieveMultipleTimelinesRequest.getPageRequest().getPageSize() + 1,
                            retrieveMultipleTimelinesRequest.getTimestampBefore()) :
                    this.timelineService.retrieveByUserIdAndCreatedTimestamp(
                            retrieveMultipleTimelinesRequest.getUserId(),
                            retrieveMultipleTimelinesRequest.getTimestampAfter(),
                            retrieveMultipleTimelinesRequest.getPageRequest().getPageSize() + 1,
                            null
                    ))
                .map(this.timelineMessageFactory::valueOf)
                .collectList()
                .zipWith(request)
                .map(tuple2 -> {
                    CommonStatus commonStatus = this.commonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
                    return TimelinesResponse.newBuilder()
                            .setStatus(commonStatus)
                            .addAllTimelines(tuple2.getT1().stream()
                                    .limit(tuple2.getT2().getPageRequest().getPageSize())
                                    .collect(Collectors.toList()))
                            .setHasMore(tuple2.getT1().size() > tuple2.getT2().getPageRequest().getPageSize())
                            .build();
                })
                .onErrorReturn(TimelinesResponse.newBuilder()
                        .setStatus(this.commonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_TIMELINE_RETRIEVE_ERROR))
                        .build());
    }

}