package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.ReactionMessage;
import com.keepreal.madagascar.common.ReactionType;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.dtoFactory.ReactionDTOFactory;
import com.keepreal.madagascar.lemur.service.ReactionService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.ReactionApi;
import swagger.model.PostReactionRequest;
import swagger.model.ReactionResponse;
import swagger.model.ReactionsResponse;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the reaction controller.
 */
@RestController
public class ReactionController implements ReactionApi {

    private final ReactionService reactionService;
    private final ReactionDTOFactory reactionDTOFactory;

    /**
     * Constructs the reaction controller.
     * @param reactionService       {@link ReactionService}.
     * @param reactionDTOFactory    {@link ReactionDTOFactory}.
     */
    public ReactionController(ReactionService reactionService, ReactionDTOFactory reactionDTOFactory) {
        this.reactionService = reactionService;
        this.reactionDTOFactory = reactionDTOFactory;
    }

    /**
     * Implements the get reactions by feed id api.
     *
     * @param id       id (required) Feed id.
     * @param page     page number (optional, default to 0).
     * @param pageSize size of a page (optional, default to 10).
     * @return {@link ReactionsResponse}.
     */
    @Override
    public ResponseEntity<ReactionsResponse> apiV1FeedsIdReactionsGet(String id, Integer page, Integer pageSize) {
        com.keepreal.madagascar.fossa.ReactionsResponse reactionsResponse =
                this.reactionService.retrieveReactionsByFeedId(id, page, pageSize);

        ReactionsResponse response = new ReactionsResponse();
        response.setData(reactionsResponse.getReactionsList()
                .stream()
                .map(this.reactionDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(reactionsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the post or revoke reaction api.
     *
     * @param id                  id (required) Feed id.
     * @param postReactionRequest (required) {@link PostReactionRequest}.
     * @param isRevoke            whether is revoking a reaction (optional) Whether create or revoke.
     * @return {@link ReactionResponse}.
     */
    @Override
    public ResponseEntity<ReactionResponse> apiV1FeedsIdReactionsPost(
            String id,
            PostReactionRequest postReactionRequest,
            @RequestParam(value = "isRevoke", required = false, defaultValue = "false") Boolean isRevoke) {
        String userId = HttpContextUtils.getUserIdFromContext();

        ReactionMessage reactionMessage;
        if (isRevoke) {
            reactionMessage = this.reactionService.revokeReaction(
                    id, userId, postReactionRequest.getReactions().stream().map(this::convertType).collect(Collectors.toList()));
        } else {
            reactionMessage = this.reactionService.createReaction(
                    id, userId, postReactionRequest.getReactions().stream().map(this::convertType).collect(Collectors.toList()));
        }

        ReactionResponse response = new ReactionResponse();
        response.setData(this.reactionDTOFactory.valueOf(reactionMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Converts {@link swagger.model.ReactionType} into {@link ReactionType}.
     *
     * @param type {@link swagger.model.ReactionType}.
     * @return {@link ReactionType}.
     */
    private ReactionType convertType(swagger.model.ReactionType type) {
        if (Objects.isNull(type)) {
            return null;
        }

        switch (type) {
            case REACTION_LIKE:
                return ReactionType.REACTION_LIKE;
            default:
                return ReactionType.UNRECOGNIZED;
        }
    }

}
