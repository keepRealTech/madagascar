package com.keepreal.madagascar.hoopoe.grpcController;

import com.keepreal.madagascar.hoopoe.BannerResponse;
import com.keepreal.madagascar.hoopoe.BannerServiceGrpc;
import com.keepreal.madagascar.hoopoe.RetrieveBannerRequest;
import com.keepreal.madagascar.hoopoe.dao.ActivityRepository;
import com.keepreal.madagascar.hoopoe.model.Activity;
import com.keepreal.madagascar.hoopoe.service.BannerService;
import com.keepreal.madagascar.hoopoe.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the banner GRpc controller.
 */
@Slf4j
@GRpcService
public class BannerGRpcController extends BannerServiceGrpc.BannerServiceImplBase {
    private final BannerService bannerService;

    /**
     * Constructs the banner GRpc controller.
     *
     * @param bannerService {@link BannerService}
     */
    public BannerGRpcController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    /**
     * 根据类型 获取banner
     *
     * @param request {@link RetrieveBannerRequest}
     * @param responseObserver {@link BannerResponse}
     */
    @Override
    public void retrieveBanner(RetrieveBannerRequest request, StreamObserver<BannerResponse> responseObserver) {
        List<Activity> banners = this.bannerService.findBannersByType(request.getBannerType());
        BannerResponse response = BannerResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .addAllBanners(banners.stream().map(this.bannerService::getBannerMessage).collect(Collectors.toList()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}