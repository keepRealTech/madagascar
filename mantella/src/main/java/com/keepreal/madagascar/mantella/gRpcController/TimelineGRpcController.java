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

/**
 * Represents the timeline grpc controller.
 */
@GRpcService
public class TimelineGRpcController extends ReactorTimelineServiceGrpc.TimelineServiceImplBase {

    private final TimelineService timelineService;
    private final TimelineMessageFactory timelineMessageFactory;
    private final CommonStatusUtils commonStatusUtils;

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
                this.timelineService.retrieveByUserIdAndCreatedTimestampAfter(
                        retrieveMultipleTimelinesRequest.getUserId(),
                        retrieveMultipleTimelinesRequest.getCreatedAfter(),
                        retrieveMultipleTimelinesRequest.getPageRequest().getPageSize()))
                .map(this.timelineMessageFactory::valueOf)
                .collectList()
                .map(timelineMessages -> {
                    CommonStatus commonStatus = this.commonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
                    return TimelinesResponse.newBuilder()
                            .setStatus(commonStatus)
                            .addAllTimelines(timelineMessages)
                            .build();
                });
    }

}