package com.keepreal.madagascar.lemur.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Represents the ehcache in-mem caching service.
 */
@Service
public class EhcacheService {

    private final FeedService feedService;
    private final Cache feedExistenceCache;
    private final CommentService commentService;
    private final Cache commentExistenceCache;

    /**
     * Constructs the service.
     *
     * @param feedService           {@link FeedService}.
     * @param cacheManager          {@link CacheManager}.
     * @param commentService        {@link CommentService}.
     */
    public EhcacheService(FeedService feedService,
                          @Qualifier("ehcacheCacheManager") CacheManager cacheManager,
                          CommentService commentService) {
        this.feedService = feedService;
        this.feedExistenceCache = cacheManager.getCache("feed-existence");
        this.commentService = commentService;
        this.commentExistenceCache = cacheManager.getCache("comment-existence");
    }

    /**
     * Checks if a feed is deleted by id.
     *
     * @param feedId Feed id.
     * @return True if deleted.
     */
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

    public boolean checkCommentDeleted(String commentId) {
        if (Boolean.TRUE.equals(this.commentExistenceCache.get(commentId, Boolean.class))) {
            return true;
        }

        if (!this.commentService.retrieveCommentById(commentId).getIsDeleted()) {
            return false;
        }

        this.feedExistenceCache.put(commentId, true);

        return true;
    }

}
