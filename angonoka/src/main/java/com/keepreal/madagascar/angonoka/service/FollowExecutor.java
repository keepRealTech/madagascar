package com.keepreal.madagascar.angonoka.service;

import com.keepreal.madagascar.angonoka.CancelFollowRequest;
import com.keepreal.madagascar.angonoka.CancelFollowResponse;
import com.keepreal.madagascar.angonoka.FollowRequest;
import com.keepreal.madagascar.angonoka.FollowResponse;

public interface FollowExecutor {
    /**
     * follow social platform.
     *
     * @param followRequest Follow request {@link FollowRequest}.
     * @return Follow response {@link FollowResponse}.
     */
    FollowResponse follow(FollowRequest followRequest);

    /**
     * cancel follow social platform.
     *
     * @param cancelFollowRequest Cancel follow request {@link CancelFollowRequest}.
     * @return Cancel follow response {@link CancelFollowResponse}.
     */
    CancelFollowResponse cancelFollow(CancelFollowRequest cancelFollowRequest);

}
