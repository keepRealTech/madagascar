package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.ReactionMessage;
import org.springframework.stereotype.Component;
import swagger.model.ReactionDTO;
import swagger.model.ReactionType;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the reaction dto factory.
 */
@Component
public class ReactionDTOFactory {

    /**
     * Converts the {@link ReactionMessage} to {@link ReactionDTO}.
     *
     * @param reaction {@link ReactionMessage}.
     * @return {@link ReactionDTO}.
     */
    public ReactionDTO valueOf(ReactionMessage reaction) {
        if (Objects.isNull(reaction)) {
            return null;
        }

        ReactionDTO reactionDTO = new ReactionDTO();
        reactionDTO.setId(reaction.getId());
        reactionDTO.setFeedId(reaction.getFeedId());
        reactionDTO.setUserId(reaction.getUserId());
        reactionDTO.setReactionType(reaction.getReactionTypeList()
                .stream()
                .map(this::convertType)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        return reactionDTO;
    }

    /**
     * Converts the {@link com.keepreal.madagascar.common.ReactionType} into {@link ReactionType}.
     *
     * @param reactionType {@link com.keepreal.madagascar.common.ReactionType}.
     * @return {@link ReactionType}.
     */
    private ReactionType convertType(com.keepreal.madagascar.common.ReactionType reactionType) {
        if (Objects.isNull(reactionType)) {
            return null;
        }

        switch (reactionType) {
            case REACTION_LIKE:
                return ReactionType.REACTION_LIKE;
            default:
                return null;
        }
    }

}
