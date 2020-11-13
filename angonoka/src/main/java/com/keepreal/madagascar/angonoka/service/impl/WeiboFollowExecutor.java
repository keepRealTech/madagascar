package com.keepreal.madagascar.angonoka.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.keepreal.madagascar.angonoka.CancelFollowRequest;
import com.keepreal.madagascar.angonoka.CancelFollowResponse;
import com.keepreal.madagascar.angonoka.FollowRequest;
import com.keepreal.madagascar.angonoka.FollowResponse;
import com.keepreal.madagascar.angonoka.FollowState;
import com.keepreal.madagascar.angonoka.FollowType;
import com.keepreal.madagascar.angonoka.api.WeiboApi;
import com.keepreal.madagascar.angonoka.config.WeiboBusinessConfig;
import com.keepreal.madagascar.angonoka.model.SuperFollow;
import com.keepreal.madagascar.angonoka.service.FollowExecutor;
import com.keepreal.madagascar.angonoka.service.FollowService;
import com.keepreal.madagascar.angonoka.util.CommonStatusUtils;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Objects;

/**
 * Represents the weibo follow executor selector.
 */
@Slf4j
public class WeiboFollowExecutor implements FollowExecutor {

    private final RestTemplate restTemplate;
    private final WeiboBusinessConfig weiboBusinessConfig;
    private final Gson gson;
    private final FollowService followService;

    /**
     * Constructs the follow executor.
     *
     * @param restTemplate {@link RestTemplate}
     * @param weiboBusinessConfig {@link WeiboBusinessConfig}
     * @param gson {@link Gson}
     */
    WeiboFollowExecutor(RestTemplate restTemplate,
                        WeiboBusinessConfig weiboBusinessConfig,
                        Gson gson,
                        FollowService followService) {
        this.restTemplate = restTemplate;
        this.weiboBusinessConfig = weiboBusinessConfig;
        this.gson = gson;
        this.followService = followService;
    }

    /**
     * 增加微博商业数据订阅
     *
     * @param followRequest Login request {@link FollowRequest}.
     * @return {@link FollowResponse}
     */
    @Override
    public FollowResponse follow(FollowRequest followRequest) {
        FollowResponse.Builder builder = FollowResponse.newBuilder();

        if (!followRequest.hasWeiboFollowPayload()) {
            return FollowResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_INVALID_ARGUMENT))
                    .build();
        }

        String id = followRequest.getWeiboFollowPayload().getId();
        String userId = followRequest.getWeiboFollowPayload().getUserId();
        String islandId = followRequest.getWeiboFollowPayload().getIslandId();

        ResponseEntity<HashMap> responseEntity = this.restTemplate.postForEntity(String.format(WeiboApi.ADD_SUBSCRIBE,
                weiboBusinessConfig.getAppKey(),
                weiboBusinessConfig.getSubId(),
                id),
                null,
                HashMap.class);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_RPC_ERROR)).build();
        }

        JsonObject body = this.gson.toJsonTree(responseEntity.getBody()).getAsJsonObject();

        if (Objects.isNull(body)) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_RPC_ERROR)).build();
        }

        if (Objects.isNull(body.get("result"))) {
            if (211118 == body.get("error_code").getAsLong()) {
                return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_ACCOUNT_NOT_FOUND)).build();
            }
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_RPC_ERROR)).build();
        }

        boolean result = body.get("result").getAsBoolean();

        if (!result) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_RPC_ERROR)).build();
        }

        this.followService.createSuperFollow(userId, islandId, id, FollowType.FOLLOW_WEIBO);

        return builder.setStatus(CommonStatusUtils.getSuccStatus()).build();
    }

    /**
     * 删除微博商业数据订阅
     *
     * @param cancelFollowRequest Cancel follow request {@link CancelFollowRequest}.
     * @return {@link CancelFollowResponse}
     */
    @Override
    public CancelFollowResponse cancelFollow(CancelFollowRequest cancelFollowRequest) {
        CancelFollowResponse.Builder builder = CancelFollowResponse.newBuilder();

        String hostId = cancelFollowRequest.getHostId();
        String islandId = cancelFollowRequest.getIslandId();
        FollowType followType = cancelFollowRequest.getFollowType();

        SuperFollow superFollow = this.followService.retrieveSuperFollowByHostId(hostId, followType);
        if (Objects.isNull(superFollow)) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUPER_FOLLOW_NOT_FOUND)).build();
        }
        String weiboUid = superFollow.getPlatformId();

        ResponseEntity<HashMap> responseEntity = this.restTemplate.postForEntity(String.format(WeiboApi.DELETE_SUBSCRIBE,
                weiboBusinessConfig.getAppKey(),
                weiboBusinessConfig.getSubId(),
                weiboUid),
                null,
                HashMap.class);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_RPC_ERROR)).build();
        }

        JsonObject body = this.gson.toJsonTree(responseEntity.getBody()).getAsJsonObject();

        if (Objects.isNull(body)) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_RPC_ERROR)).build();
        }

        if (Objects.isNull(body.get("result"))) {
            if (211118 == body.get("error_code").getAsLong()) {
                return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_ACCOUNT_NOT_FOUND)).build();
            }
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_RPC_ERROR)).build();
        }

        boolean result = body.get("result").getAsBoolean();

        if (!result) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_RPC_ERROR)).build();
        }

        superFollow.setState(FollowState.NONE_VALUE);
        this.followService.updateSuperFollow(superFollow);

        return builder.setStatus(CommonStatusUtils.getSuccStatus()).build();
    }

}
