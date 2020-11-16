package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.enums.BannerType;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.hoopoe.BannerMessage;
import com.keepreal.madagascar.lemur.dtoFactory.BannerDTOFactory;
import com.keepreal.madagascar.lemur.service.BannerService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.BannerApi;
import swagger.model.BannersResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the banner controller.
 */
@RestController
public class BannerController implements BannerApi {
    private final BannerService bannerService;
    private final IslandService islandService;
    private final BannerDTOFactory bannerDTOFactory;

    /**
     * Constructs the banner controller.
     *  @param bannerService {@link BannerService}
     * @param islandService {@link IslandService}
     * @param bannerDTOFactory {@link BannerDTOFactory}
     */
    public BannerController(BannerService bannerService,
                            IslandService islandService,
                            BannerDTOFactory bannerDTOFactory) {
        this.bannerService = bannerService;
        this.islandService = islandService;
        this.bannerDTOFactory = bannerDTOFactory;
    }

    /**
     * Implements the creator banner get api.
     *
     * @return {@link BannersResponse}
     */
    @Override
    public ResponseEntity<BannersResponse> apiV1BannersCreatorGet() {
        String userId = HttpContextUtils.getUserIdFromContext();

        List<IslandMessage> islandMessages = this.islandService.retrieveIslandsByHostId(userId);
        if (CollectionUtils.isEmpty(islandMessages)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<BannerMessage> bannerMessages = this.bannerService.retrieveBanners(userId, BannerType.CREATOR);

        BannersResponse response = new BannersResponse();
        response.setData(bannerMessages.stream().map(this.bannerDTOFactory::valueOf).collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the super follow banner get api.
     *
     * @return {@link BannersResponse}
     */
    @Override
    public ResponseEntity<BannersResponse> apiV1BannersFollowGet() {
        String userId = HttpContextUtils.getUserIdFromContext();

        List<IslandMessage> islandMessages = this.islandService.retrieveIslandsByHostId(userId);
        if (CollectionUtils.isEmpty(islandMessages)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<BannerMessage> bannerMessages = this.bannerService.retrieveBanners(userId, BannerType.SUPER_FOLLOW);

        BannersResponse response = new BannersResponse();
        response.setData(bannerMessages.stream().map(this.bannerDTOFactory::valueOf).collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
