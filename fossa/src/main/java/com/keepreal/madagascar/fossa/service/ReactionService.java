package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.ReactionType;
import com.keepreal.madagascar.fossa.dao.ReactionRepository;
import com.keepreal.madagascar.fossa.model.ReactionInfo;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Represents the reaction service.
 */
@Service
public class ReactionService {

    private final ReactionRepository reactionRepository;

    public ReactionService(ReactionRepository reactionRepository) {
        this.reactionRepository = reactionRepository;
    }

    public Set<ReactionInfo> retrieveReactionsByFeedIdsAndUserId(Iterable<String> feedIds, String userId) {
        return this.reactionRepository.findByFeedIdInAndUserIdAndReactionTypeListContains(feedIds,
                userId,
                ReactionType.REACTION_LIKE_VALUE);
    }

}
