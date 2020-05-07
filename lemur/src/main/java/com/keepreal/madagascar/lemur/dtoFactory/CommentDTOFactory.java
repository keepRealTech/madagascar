package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.lemur.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import swagger.model.CommentDTO;

import java.util.Objects;

/**
 * Represents the comment dto factory.
 */
@Component
public class CommentDTOFactory {

    private final UserService userService;
    private final UserDTOFactory userDTOFactory;

    /**
     * Constructs the comment dto factory.
     *
     * @param userService    {@link UserService}.
     * @param userDTOFactory {@link UserDTOFactory}.
     */
    public CommentDTOFactory(UserService userService,
                             UserDTOFactory userDTOFactory) {
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
    }

    /**
     * Converts the {@link CommentMessage} to {@link CommentDTO}.
     *
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
        commentDTO.setContent(comment.getContent());
        commentDTO.setCreatedAt(comment.getCreatedAt());

        if (!StringUtils.isEmpty(comment.getReplyToId())) {
            commentDTO.setReplyTo(this.userDTOFactory.briefValueOf(
                    this.userService.retrieveUserById(comment.getReplyToId())));
        }

        commentDTO.setUser(this.userDTOFactory.briefValueOf(
                this.userService.retrieveUserById(comment.getUserId())));

        return commentDTO;
    }

}
