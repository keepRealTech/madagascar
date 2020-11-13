package com.keepreal.madagascar.angonoka.service.impl;

import com.keepreal.madagascar.angonoka.CancelFollowRequest;
import com.keepreal.madagascar.angonoka.CancelFollowResponse;
import com.keepreal.madagascar.angonoka.FollowRequest;
import com.keepreal.madagascar.angonoka.FollowResponse;
import com.keepreal.madagascar.angonoka.service.FollowExecutor;
import com.keepreal.madagascar.angonoka.util.CommonStatusUtils;
import com.keepreal.madagascar.common.exceptions.ErrorCode;

public class DefaultFollowExecutor implements FollowExecutor {

    @Override
    public FollowResponse follow(FollowRequest followRequest) {
        return FollowResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FOLLOW_TYPE_NOT_SUPPORT))
                .build();
    }

    @Override
    public CancelFollowResponse cancelFollow(CancelFollowRequest cancelFollowRequest) {
        return CancelFollowResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FOLLOW_TYPE_NOT_SUPPORT))
                .build();
    }

}
