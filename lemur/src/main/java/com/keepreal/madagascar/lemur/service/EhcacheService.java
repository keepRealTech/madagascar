package com.keepreal.madagascar.lemur.service;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class EhcacheService {

    private final FeedService feedService;
    private final Cache feedExistenceCache;

    public EhcacheService(FeedService feedService, CacheManager cacheManager) {
        this.feedService = feedService;
        this.feedExistenceCache = cacheManager.getCache("feed-existence");
    }

    public boolean checkFeedDeleted(String feedId) {
        if (Boolean.TRUE.equals(this.feedExistenceCache.get(feedId, Boolean.class))) {
            return true;
        }

        if (!this.feedService.checkDeleted(feedId)) {
            return false;
        }

        this.feedExistenceCache.put(feedId, true);

        return true;
    }

}
