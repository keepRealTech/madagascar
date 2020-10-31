package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.vanga.RetrieveSponsorHistoryCountRequest;
import com.keepreal.madagascar.vanga.RetrieveSponsorHistoryCountResponse;
import com.keepreal.madagascar.vanga.RetrieveSponsorHistoryRequest;
import com.keepreal.madagascar.vanga.RetrieveSponsorHistoryResponse;
import com.keepreal.madagascar.vanga.SponsorHistoryServiceGrpc;
import com.keepreal.madagascar.vanga.model.SponsorHistory;
import com.keepreal.madagascar.vanga.service.SponsorHistoryService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import com.keepreal.madagascar.vanga.util.PaginationUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.stream.Collectors;

/**
 * Represent the sponsor history grpc controller.
 */
@GRpcService
public class SponsorHistoryGRpcController extends SponsorHistoryServiceGrpc.SponsorHistoryServiceImplBase {

    private final SponsorHistoryService sponsorHistoryService;

    /**
     * Constructs the sponsor history grpc controller
     *
     * @param sponsorHistoryService {@link SponsorHistoryService}
     */
    public SponsorHistoryGRpcController(SponsorHistoryService sponsorHistoryService) {
        this.sponsorHistoryService = sponsorHistoryService;
    }


    /**
     * 获取支持一下赞助历史
     *
     * @param request {@link RetrieveSponsorHistoryRequest}
     * @param responseObserver {@link RetrieveSponsorHistoryResponse}
     */
    @Override
    public void retrieveSponsorHistoryByIslandId(RetrieveSponsorHistoryRequest request, StreamObserver<RetrieveSponsorHistoryResponse> responseObserver) {
        String islandId = request.getIslandId();
        int page = request.getPageRequest().getPage();
        int pageSize = request.getPageRequest().getPageSize();
        Page<SponsorHistory> sponsorHistory = this.sponsorHistoryService.retrieveSponsorHistoryOrderByCreatedTimeDesc(islandId, PageRequest.of(page, pageSize));
        RetrieveSponsorHistoryResponse response = RetrieveSponsorHistoryResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setPageResponse(PaginationUtils.valueOf(sponsorHistory, request.getPageRequest()))
                .addAllSponsorHistory(sponsorHistory.stream().map(this.sponsorHistoryService::getSponsorHistoryMessage).collect(Collectors.toList()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 获取支持一下历史总数
     *
     * @param request {@link RetrieveSponsorHistoryCountRequest}
     * @param responseObserver {@link RetrieveSponsorHistoryCountResponse}
     */
    @Override
    public void retrieveSponsorHistoryCountByIslandId(RetrieveSponsorHistoryCountRequest request, StreamObserver<RetrieveSponsorHistoryCountResponse> responseObserver) {
        Long count = this.sponsorHistoryService.retrieveSponsorHistoryCountByIslandId(request.getIslandId());
        RetrieveSponsorHistoryCountResponse response = RetrieveSponsorHistoryCountResponse.newBuilder()
                .setCount(count)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
