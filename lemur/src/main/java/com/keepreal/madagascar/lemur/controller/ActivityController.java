package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.hoopoe.ActiveBannerMessage;
import com.keepreal.madagascar.lemur.dtoFactory.ActivityDTOFactory;
import com.keepreal.madagascar.lemur.service.ActivityService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final ActivityDTOFactory activityDTOFactory;

    /**
     * Constructs the activity controller.
     *
     * @param activityService {@link ActivityService}
     * @param activityDTOFactory {@link ActivityDTOFactory}
     */
    public ActivityController(ActivityService activityService,
                              ActivityDTOFactory activityDTOFactory) {
        this.activityService = activityService;
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
        List<ActiveBannerMessage> bannerList = this.activityService.retrieveActiveBanner(userId);

        ActivitiesResponse response = new ActivitiesResponse();
        response.setData(bannerList.stream().map(this.activityDTOFactory::valueOf).collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
