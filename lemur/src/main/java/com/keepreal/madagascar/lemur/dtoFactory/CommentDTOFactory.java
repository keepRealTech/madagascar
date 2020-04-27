package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.CommentMessage;
import org.springframework.stereotype.Component;
import swagger.model.CommentDTO;

import java.util.Objects;

/**
 * Represents the comment dto factory.
 */
@Component
public class CommentDTOFactory {

    /**
     * Converts the {@link CommentMessage} to {@link CommentDTO}.
     * @param comment {@link CommentMessage}.
     * @return {@link CommentDTO}.
     */
    public CommentDTO valueOf(CommentMessage comment) {
        if (Objects.isNull(comment)) {
            return null;
        }

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setFeedId(comment.getFeedId());
        commentDTO.setUserId(comment.getUserId());
        commentDTO.setReplyToId(comment.getReplyToId());
        commentDTO.setContent(comment.getContent());
        commentDTO.setCreatedAt(comment.getCreatedAt());

        return commentDTO;
    }

}
