package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.hoopoe.ActiveBannerMessage;
import com.keepreal.madagascar.lemur.dtoFactory.ActivityDTOFactory;
import com.keepreal.madagascar.lemur.service.ActivityService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.ActivityApi;
import swagger.model.ActivitiesResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the activity controller.
 */
@RestController
public class ActivityController implements ActivityApi {

    private final ActivityService activityService;
    private final IslandService islandService;
    private final ActivityDTOFactory activityDTOFactory;

    /**
     * Constructs the activity controller.
     * @param activityService       {@link ActivityService}
     * @param islandService         {@link IslandService}
     * @param activityDTOFactory    {@link ActivityDTOFactory}
     */
    public ActivityController(ActivityService activityService,
                              IslandService islandService,
                              ActivityDTOFactory activityDTOFactory) {
        this.activityService = activityService;
        this.islandService = islandService;
        this.activityDTOFactory = activityDTOFactory;
    }

    /**
     * 获取活动banner
     *
     * @return {@link ActivitiesResponse}
     */
    @Override
    public ResponseEntity<ActivitiesResponse> apiV1ActivitiesBannersGet() {
        String userId = HttpContextUtils.getUserIdFromContext();
        boolean isIslandHost = false;

        List<IslandMessage> islandMessages = this.islandService.retrieveIslandsByHostId(userId);
        if (!CollectionUtils.isEmpty(islandMessages)) {
            isIslandHost = true;
        }

        List<ActiveBannerMessage> bannerList = this.activityService.retrieveActiveBanner(isIslandHost);

        ActivitiesResponse response = new ActivitiesResponse();
        response.setData(bannerList.stream().map(this.activityDTOFactory::valueOf).collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
