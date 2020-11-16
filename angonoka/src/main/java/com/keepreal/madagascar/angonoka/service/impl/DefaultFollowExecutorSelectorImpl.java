package com.keepreal.madagascar.angonoka.service.impl;

import com.google.gson.Gson;
import com.keepreal.madagascar.angonoka.FollowType;
import com.keepreal.madagascar.angonoka.config.WeiboBusinessConfig;
import com.keepreal.madagascar.angonoka.service.FollowExecutor;
import com.keepreal.madagascar.angonoka.service.FollowExecutorSelector;
import com.keepreal.madagascar.angonoka.service.FollowService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Represents the default follow executor selector.
 */
@Component
public class DefaultFollowExecutorSelectorImpl implements FollowExecutorSelector {
    private final RestTemplate restTemplate;
    private final WeiboBusinessConfig weiboBusinessConfig;
    private final Gson gson;
    private final FollowService followService;

    /**
     * Constructs the {@link DefaultFollowExecutorSelectorImpl}
     *
     * @param restTemplate {@link RestTemplate}
     * @param weiboBusinessConfig {@link WeiboBusinessConfig}
     * @param followService {@link FollowService}
     */
    public DefaultFollowExecutorSelectorImpl(RestTemplate restTemplate,
                                             WeiboBusinessConfig weiboBusinessConfig,
                                             FollowService followService) {
        this.restTemplate = restTemplate;
        this.weiboBusinessConfig = weiboBusinessConfig;
        this.followService = followService;
        this.gson = new Gson();
    }

    /**
     * Selects the follow executor.
     *
     * @param followType {@link FollowType}.
     * @return {@link FollowExecutor}.
     */
    @Override
    public FollowExecutor select(FollowType followType) {
        switch (followType) {
            case FOLLOW_WEIBO:
                return new WeiboFollowExecutor(
                        this.restTemplate,
                        this.weiboBusinessConfig,
                        this.gson,
                        this.followService);
            case FOLLOW_TIKTOK:
                return new DefaultFollowExecutor();
            case FOLLOW_BILIBILI:
                return new DefaultFollowExecutor();
            default:
                return new DefaultFollowExecutor();
        }
    }
}
