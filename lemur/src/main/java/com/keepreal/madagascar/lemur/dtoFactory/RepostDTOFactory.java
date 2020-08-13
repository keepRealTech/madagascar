package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.fossa.FeedRepostMessage;
import com.keepreal.madagascar.fossa.IslandRepostMessage;
import com.keepreal.madagascar.fossa.ResolveRepostCodeResponse;
import org.springframework.stereotype.Component;
import swagger.model.CodeItemDTO;
import swagger.model.RepostDTO;
import swagger.model.ResolveIslandRepostCodeDTO;

import java.util.Objects;

/**
 * Represents the repost dto factory.
 */
@Component
public class RepostDTOFactory {

    /**
     * Converts {@link IslandRepostMessage} into {@link RepostDTO}.
     *
     * @param islandRepost {@link IslandRepostMessage}.
     * @return {@link RepostDTO}.
     */
    public RepostDTO valueOf(IslandRepostMessage islandRepost) {
        if (Objects.isNull(islandRepost) || Objects.isNull(islandRepost.getIslandRepost())) {
            return null;
        }

        RepostDTO repostDTO = new RepostDTO();
        repostDTO.setIslandId(islandRepost.getIslandId());
        repostDTO.setId(islandRepost.getIslandRepost().getId());
        repostDTO.setUserId(islandRepost.getIslandRepost().getUserId());
        repostDTO.setIsSuccessful(islandRepost.getIslandRepost().getIsSuccessful());
        repostDTO.setContent(islandRepost.getIslandRepost().getContent());
        repostDTO.setCreatedAt(islandRepost.getIslandRepost().getCreatedAt());

        return repostDTO;
    }

    /**
     * Converts {@link FeedRepostMessage} into {@link RepostDTO}.
     *
     * @param feedRepost {@link FeedRepostMessage}.
     * @return {@link RepostDTO}.
     */
    public RepostDTO valueOf(FeedRepostMessage feedRepost) {
        if (Objects.isNull(feedRepost) || Objects.isNull(feedRepost.getFeedRepost())) {
            return null;
        }

        RepostDTO repostDTO = new RepostDTO();
        repostDTO.setFeedId(feedRepost.getFeedId());
        repostDTO.setId(feedRepost.getFeedRepost().getId());
        repostDTO.setUserId(feedRepost.getFeedRepost().getUserId());
        repostDTO.setIsSuccessful(feedRepost.getFeedRepost().getIsSuccessful());
        repostDTO.setContent(feedRepost.getFeedRepost().getContent());
        repostDTO.setCreatedAt(feedRepost.getFeedRepost().getCreatedAt());

        return repostDTO;
    }

    public ResolveIslandRepostCodeDTO codeValueOf(ResolveRepostCodeResponse response) {
        ResolveIslandRepostCodeDTO dto = new ResolveIslandRepostCodeDTO();

        CodeItemDTO codeItemDTO = new CodeItemDTO();
        codeItemDTO.setIslandId(response.getIslandId());
        if (response.hasSecret()) {
            codeItemDTO.setSecret(response.getSecret().getValue());
        }
        dto.setParams(codeItemDTO);
        dto.setRedirectUrl(response.getRedirectUrl());

        return dto;
    }

}
