package com.keepreal.madagascar.angonoka.service.impl;

import com.google.gson.Gson;
import com.keepreal.madagascar.angonoka.FollowType;
import com.keepreal.madagascar.angonoka.config.WeiboBusinessConfig;
import com.keepreal.madagascar.angonoka.service.FollowExecutor;
import com.keepreal.madagascar.angonoka.service.FollowExecutorSelector;
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

    /**
     * Constructs the {@link DefaultFollowExecutorSelectorImpl}
     *
     * @param restTemplate {@link RestTemplate}
     * @param weiboBusinessConfig {@link WeiboBusinessConfig}
     */
    public DefaultFollowExecutorSelectorImpl(RestTemplate restTemplate,
                                             WeiboBusinessConfig weiboBusinessConfig) {
        this.restTemplate = restTemplate;
        this.weiboBusinessConfig = weiboBusinessConfig;
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
            case FOLLOW_TYPE_NONE:
                return new WeiboFollowExecutor(
                        this.restTemplate,
                        this.weiboBusinessConfig,
                        this.gson);
            case FOLLOW_WEIBO:
                break;
            case FOLLOW_TIKTOK:
                break;
            case FOLLOW_BILIBILI:
                break;
            case UNRECOGNIZED:
                break;
        }
        return null;
    }
}
