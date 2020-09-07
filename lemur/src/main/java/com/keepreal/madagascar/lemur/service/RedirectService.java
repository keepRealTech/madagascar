package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Represents the redirect service.
 */
@Service
public class RedirectService {

    private final RMapCache<String, String> redirectCache;

    /**
     * Constructs the redirect service.
     *
     * @param redissonClient {@link RedissonClient}.
     */
    public RedirectService(RedissonClient redissonClient) {
        this.redirectCache = redissonClient.getMapCache("ShortLinks", StringCodec.INSTANCE);
    }

    /**
     * Retrieves the redirect url for given short code.
     *
     * @param shortCode Short code.
     * @return Redirect url.
     */
    public String getRedirectUrl(String shortCode) {
        String redirectRoute = this.redirectCache.get(shortCode);
        if (Objects.isNull(redirectRoute)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_REDIS_SHORTURL_NOT_FOUND_ERROR);
        }

        return redirectRoute;
    }

    /**
     * Inserts a new short code vs redirect url pair.
     *
     * @param shortCode  Short code.
     * @param linkUrl    Link url.
     */
    public void insertRedirectUrl(String shortCode, String linkUrl) {
        if (Objects.isNull(this.redirectCache.get(shortCode))
                || !this.redirectCache.fastPut(shortCode, linkUrl, 300, TimeUnit.DAYS)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_REDIS_FAILED_PUT_ERROR);
        }
    }

}
