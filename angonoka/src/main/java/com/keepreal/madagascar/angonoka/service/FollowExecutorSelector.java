package com.keepreal.madagascar.angonoka.service;

import com.keepreal.madagascar.angonoka.FollowType;

public interface FollowExecutorSelector {
    /**
     * Selects the follow executor for the given follow type.
     *
     * @param followType {@link FollowType}.
     * @return {@link FollowExecutor}.
     */
    FollowExecutor select(FollowType followType);
}
