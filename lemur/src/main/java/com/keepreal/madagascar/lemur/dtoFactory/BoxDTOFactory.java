package com.keepreal.madagascar.lemur.dtoFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.QuestionMessage;
import com.keepreal.madagascar.fossa.BoxMessage;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.service.SubscribeMembershipService;
import com.keepreal.madagascar.lemur.service.UserService;
import org.springframework.stereotype.Component;
import swagger.model.AnswerDTO;
import swagger.model.BoxAccessDTO;
import swagger.model.BoxDTO;
import swagger.model.BriefUserDTO;
import swagger.model.FullQuestionDTO;
import swagger.model.QuestionDTO;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class BoxDTOFactory {

    private final MembershipDTOFactory membershipDTOFactory;
    private final MembershipService membershipService;
    private final SubscribeMembershipService subscribeMembershipService;
    private final UserService userService;
    private final UserDTOFactory userDTOFactory;
    private final CommentDTOFactory commentDTOFactory;

    public BoxDTOFactory(MembershipDTOFactory membershipDTOFactory,
                         MembershipService membershipService,
                         SubscribeMembershipService subscribeMembershipService,
                         UserService userService,
                         UserDTOFactory userDTOFactory,
                         CommentDTOFactory commentDTOFactory) {
        this.membershipDTOFactory = membershipDTOFactory;
        this.membershipService = membershipService;
        this.subscribeMembershipService = subscribeMembershipService;
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
        this.commentDTOFactory = commentDTOFactory;
    }

    public BoxDTO valueOf(BoxMessage boxMessage, String userId) {
        if (Objects.isNull(boxMessage)) {
            return null;
        }

        BoxDTO dto = new BoxDTO();
        dto.setIslandId(boxMessage.getIsland());
        dto.setAnsweredQuestionsCount(boxMessage.getAnsweredQuestionCount());

        List<String> myMembershipIds = this.subscribeMembershipService.retrieveSubscribedMembershipsByIslandIdAndUserId(boxMessage.getIsland(), userId);
        ProtocolStringList membershipIdsList = boxMessage.getMembershipIdsList();
        dto.setHasSubmitAccess(membershipIdsList.stream().anyMatch(myMembershipIds::contains));

        return dto;
    }

    public BoxAccessDTO valueOf(BoxMessage boxMessage) {
        if (Objects.isNull(boxMessage)) {
            return null;
        }

        BoxAccessDTO dto = new BoxAccessDTO();
        dto.setEnabled(boxMessage.getEnabled());
        dto.setMembershipIds(boxMessage.getMembershipIdsList());

        return dto;
    }

    public QuestionDTO valueOf(FeedMessage feedMessage) {
        if (Objects.isNull(feedMessage)) {
            return null;
        }

        QuestionMessage question = feedMessage.getQuestion();

        QuestionDTO dto = new QuestionDTO();
        dto.setId(feedMessage.getId());
        dto.setIslandId(feedMessage.getIslandId());
        dto.setText(feedMessage.getText());
        dto.setHasAccess(feedMessage.getIsAccess());
        dto.setHasExpired(feedMessage.getCreatedAt() > System.currentTimeMillis());
        dto.setHasPaid(question.getPriceInCents() > 0);
        dto.setPublicVisible(question.getPublicVisible().getValue());
        dto.setHasAnswer(question.hasAnswer());
        dto.setIsLiked(feedMessage.getIsLiked());
        dto.setVisibleMembershipIds(feedMessage.getMembershipIdList());
        dto.setCommentsCount(feedMessage.getCommentsCount());
        dto.setLikesCount(feedMessage.getLikesCount());
        dto.setPriceInCents(question.getPriceInCents());
        dto.setCreatedAt(feedMessage.getCreatedAt());
        return dto;
    }

    public FullQuestionDTO fullValueOf(FeedMessage feedMessage) {
        if (Objects.isNull(feedMessage)) {
            return null;
        }

        QuestionMessage question = feedMessage.getQuestion();

        FullQuestionDTO dto = new FullQuestionDTO();
        dto.setId(feedMessage.getId());
        dto.setIslandId(feedMessage.getIslandId());
        dto.setText(feedMessage.getText());
        dto.setIsLiked(feedMessage.getIsLiked());
        dto.setHasAccess(feedMessage.getIsAccess());
        dto.setHasExpired(feedMessage.getCreatedAt() > System.currentTimeMillis());
        dto.setHasPaid(question.getPriceInCents() > 0);
        dto.setPublicVisible(question.getPublicVisible().getValue());
        dto.setHasAnswer(question.hasAnswer());
        dto.setVisibleMembershipIds(feedMessage.getMembershipIdList());
        dto.setCommentsCount(feedMessage.getCommentsCount());
        dto.setLikesCount(feedMessage.getLikesCount());
        dto.setPriceInCents(question.getPriceInCents());
        dto.setCreatedAt(feedMessage.getCreatedAt());

        if (question.hasAnswer()) {
            dto.setAnswer(question.getAnswer().getValue());
            dto.setAnswerer(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(question.getAnswerUserId().getValue())));
            dto.setAnsweredAt(question.getAnsweredAt().getValue());
        }

        dto.setComments(feedMessage.getLastCommentsList()
                .stream()
                .map(this.commentDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        return dto;
    }

}
