package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.angonoka.CancelFollowRequest;
import com.keepreal.madagascar.angonoka.FollowRequest;
import com.keepreal.madagascar.angonoka.RetrieveAllSuperFollowResponse;
import com.keepreal.madagascar.angonoka.SuperFollowMessage;
import com.keepreal.madagascar.angonoka.WeiboFollowPayload;
import com.keepreal.madagascar.angonoka.WeiboProfileMessage;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.dtoFactory.FollowDTOFactory;
import com.keepreal.madagascar.lemur.service.FollowService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.FollowApi;
import swagger.model.DummyResponse;
import swagger.model.FollowResponse;
import swagger.model.FollowType;
import swagger.model.PostFollowRequest;
import swagger.model.SingleFollowResponse;
import swagger.model.WeiboProfileResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Represents the follow controller.
 */
@RestController
public class FollowController implements FollowApi {

    private final FollowService followService;
    private final IslandService islandService;
    private final FollowDTOFactory followDTOFactory;

    /**
     * Constructs the follow controller.
     *
     * @param followService {@link FollowService}
     * @param islandService {@link IslandService}
     * @param followDTOFactory {@link FollowDTOFactory}
     */
    public FollowController(FollowService followService,
                            IslandService islandService,
                            FollowDTOFactory followDTOFactory) {
        this.followService = followService;
        this.islandService = islandService;
        this.followDTOFactory = followDTOFactory;
    }

    /**
     * Implements the weibo profile get api.
     *
     * @param name  (required) weibo nick name
     * @return {@link WeiboProfileResponse}
     */
    @Override
    public ResponseEntity<WeiboProfileResponse> apiV1FollowProfileWeiboGet(@NotNull @Valid String name) {
        WeiboProfileMessage weiboProfileMessage = this.followService.retrieveWeiboProfileByNickName(name);
        WeiboProfileResponse response = new WeiboProfileResponse();
        response.setData(this.followDTOFactory.valueOf(weiboProfileMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the follow delete api.
     *
     * @param type  (required) {@link FollowType}
     * @return {@link DummyResponse}
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1FollowSubscribeDelete(@NotNull @Valid FollowType type) {
        CancelFollowRequest.Builder builder = CancelFollowRequest.newBuilder();
        String userId = HttpContextUtils.getUserIdFromContext();
        List<IslandMessage> islandMessages = this.islandService.retrieveIslandsByHostId(userId);
        if (islandMessages.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        switch (type) {
            case WEIBO:
                builder.setFollowType(com.keepreal.madagascar.angonoka.FollowType.FOLLOW_WEIBO)
                        .setHostId(userId)
                        .setIslandId(islandMessages.get(0).getId());
                break;
            case TIKTOK:
                break;
            case BILIBILI:
                break;
            default:
                throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        }

        this.followService.cancelFollowSocialPlatform(builder.build());
        DummyResponse response = new DummyResponse();
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the follow get api.
     *
     * @return {@link FollowResponse}
     */
    @Override
    public ResponseEntity<FollowResponse> apiV1FollowSubscribeGet() {
        String userId = HttpContextUtils.getUserIdFromContext();
        RetrieveAllSuperFollowResponse messageResponse = this.followService.retrieveAllSuperFollowBotByHostId(userId);
        FollowResponse response = new FollowResponse();
        response.setData(this.followDTOFactory.valueOf(messageResponse));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the follow post api.
     *
     * @param postFollowRequest  (required) {@link PostFollowRequest}
     * @return {@link SingleFollowResponse}
     */
    @Override
    public ResponseEntity<SingleFollowResponse> apiV1FollowSubscribePost(@Valid PostFollowRequest postFollowRequest) {
        FollowRequest followRequest = null;

        String userId = HttpContextUtils.getUserIdFromContext();
        List<IslandMessage> islandMessages = this.islandService.retrieveIslandsByHostId(userId);
        if (islandMessages.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        switch (postFollowRequest.getFollowType()) {
            case WEIBO:
                if (StringUtils.isEmpty(postFollowRequest.getData().getId())) {
                    throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
                }
                followRequest = FollowRequest.newBuilder()
                        .setFollowType(com.keepreal.madagascar.angonoka.FollowType.FOLLOW_WEIBO)
                        .setWeiboFollowPayload(WeiboFollowPayload.newBuilder()
                                .setId(postFollowRequest.getData().getId())
                                .setIslandId(islandMessages.get(0).getId())
                                .setUserId(userId)
                                .build())
                        .build();
                break;
            case TIKTOK:
                throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
            case BILIBILI:
                throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        }

        SuperFollowMessage superFollowMessage = this.followService.followSocialPlatform(followRequest);
        SingleFollowResponse response = new SingleFollowResponse();
        response.setData(this.followDTOFactory.valueOf(superFollowMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
