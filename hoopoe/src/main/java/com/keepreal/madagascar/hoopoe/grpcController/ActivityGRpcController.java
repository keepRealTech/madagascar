package com.keepreal.madagascar.hoopoe.grpcController;

import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.hoopoe.ActiveBannerMessage;
import com.keepreal.madagascar.hoopoe.ActivityServiceGrpc;
import com.keepreal.madagascar.hoopoe.RetrieveActiveBannerRequest;
import com.keepreal.madagascar.hoopoe.RetrieveActiveBannerResponse;
import com.keepreal.madagascar.hoopoe.SingleBannerMessage;
import com.keepreal.madagascar.hoopoe.config.ActivityConfiguration;
import com.keepreal.madagascar.hoopoe.model.Activity;
import com.keepreal.madagascar.hoopoe.service.ActivityService;
import com.keepreal.madagascar.hoopoe.service.IslandService;
import com.keepreal.madagascar.hoopoe.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the activity GRpc controller.
 */
@Slf4j
@GRpcService
public class ActivityGRpcController extends ActivityServiceGrpc.ActivityServiceImplBase {

    private final ActivityService activityService;
    private final IslandService islandService;
    private final ActivityConfiguration activityConfiguration;

    /**
     * Constructs the activity grpc controller.
     *
     * @param activityService   {@link ActivityService}
     */
    public ActivityGRpcController(ActivityService activityService,
                                  IslandService islandService,
                                  ActivityConfiguration activityConfiguration) {
        this.activityService = activityService;
        this.islandService = islandService;
        this.activityConfiguration =activityConfiguration;
    }

    /**
     * 获取可用的banner
     *
     * @param request           {@link RetrieveActiveBannerRequest}
     * @param responseObserver  {@link RetrieveActiveBannerResponse}
     */
    @Override
    public void retrieveActiveBanner(RetrieveActiveBannerRequest request, StreamObserver<RetrieveActiveBannerResponse> responseObserver) {
        String userId = request.getUserId();
        Boolean showLabel = this.activityConfiguration.getShowLabel();
        Boolean isPublic = this.activityConfiguration.getIsPublic();
        String text = this.activityConfiguration.getText();

        RetrieveActiveBannerResponse.Builder responseBuilder = RetrieveActiveBannerResponse.newBuilder();
        ActiveBannerMessage.Builder builder = ActiveBannerMessage.newBuilder();

        if (showLabel) {
            builder.setLabel(text);
        }

        if (!isPublic) {
            List<IslandMessage> islandMessages = this.islandService.retrieveIslandsByHostId(userId);
            if (!CollectionUtils.isEmpty(islandMessages)) {
                List<Activity> activities = this.activityService.findAllAccessActivities();
                this.convertBanners(builder, activities);
            }
        } else {
            List<Activity> activities = this.activityService.findAllAccessActivities();
            this.convertBanners(builder, activities);
        }

        ArrayList<ActiveBannerMessage> list = new ArrayList<>();
        list.add(builder.build());
        responseBuilder.addAllActiveBanners(list);
        responseBuilder.setStatus(CommonStatusUtils.getSuccStatus());
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    /**
     * convert {@link Activity} to {@link SingleBannerMessage}
     *
     * @param builder       {@link ActiveBannerMessage.Builder}
     * @param activities    {@link List<Activity>}
     */
    private void convertBanners(ActiveBannerMessage.Builder builder, List<Activity> activities) {
        builder.addAllBanners(activities.stream()
                .map(activity -> SingleBannerMessage.newBuilder()
                        .setImageUri(activity.getImageUri())
                        .setRedirectUrl(activity.getRedirectUrl())
                        .build())
                .collect(Collectors.toList()));
    }

}