package com.keepreal.madagascar.lemur.dtoFactory;

import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.common.AnswerMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.fossa.BoxMessage;
import com.keepreal.madagascar.lemur.service.SubscribeMembershipService;
import com.keepreal.madagascar.lemur.service.UserService;
import org.springframework.stereotype.Component;
import swagger.model.BoxAccessDTO;
import swagger.model.BoxDTO;
import swagger.model.FullQuestionDTO;
import swagger.model.QuestionDTO;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class BoxDTOFactory {

    private final SubscribeMembershipService subscribeMembershipService;
    private final UserService userService;
    private final UserDTOFactory userDTOFactory;
    private final CommentDTOFactory commentDTOFactory;

    public BoxDTOFactory(SubscribeMembershipService subscribeMembershipService,
                         UserService userService,
                         UserDTOFactory userDTOFactory,
                         CommentDTOFactory commentDTOFactory) {
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
        dto.setBoxAccess(this.valueOf(boxMessage));

        List<String> myMembershipIds = this.subscribeMembershipService.retrieveSubscribedMembershipsByIslandIdAndUserId(boxMessage.getIsland(), userId);
        ProtocolStringList membershipIdsList = boxMessage.getMembershipIdsList();
        dto.setHasSubmitAccess(userId.equals(boxMessage.getHostId()) || membershipIdsList.stream().anyMatch(myMembershipIds::contains));

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

    public QuestionDTO valueOf(FeedMessage feedMessage, String userId) {
        if (Objects.isNull(feedMessage)) {
            return null;
        }

        AnswerMessage answer = feedMessage.getAnswer();
        answer = Objects.isNull(answer) ? AnswerMessage.getDefaultInstance() : answer;

        QuestionDTO dto = new QuestionDTO();
        dto.setId(feedMessage.getId());
        dto.setIslandId(feedMessage.getIslandId());
        dto.setText(feedMessage.getText());
        dto.setHasAccess(feedMessage.getIsAccess() || userId.equals(feedMessage.getUserId()));
        dto.setHasExpired(feedMessage.getCreatedAt() > System.currentTimeMillis());
        dto.setHasPaid(feedMessage.getPriceInCents() > 0);
        dto.setPublicVisible(answer.hasPublicVisible() ? answer.getPublicVisible().getValue() : null);
        dto.setHasAnswer(answer.hasAnswer());
        dto.setIsLiked(feedMessage.getIsLiked());
        dto.setVisibleMembershipIds(feedMessage.getMembershipIdList());
        dto.setCommentsCount(feedMessage.getCommentsCount());
        dto.setLikesCount(feedMessage.getLikesCount());
        dto.setPriceInCents(feedMessage.getPriceInCents());
        dto.setCreatedAt(feedMessage.getCreatedAt());
        return dto;
    }

    public FullQuestionDTO fullValueOf(FeedMessage feedMessage, String userId) {
        if (Objects.isNull(feedMessage)) {
            return null;
        }

        AnswerMessage answer = feedMessage.getAnswer();
        answer = Objects.isNull(answer) ? AnswerMessage.getDefaultInstance() : answer;

        FullQuestionDTO dto = new FullQuestionDTO();
        dto.setId(feedMessage.getId());
        dto.setIslandId(feedMessage.getIslandId());
        dto.setText(feedMessage.getText());
        dto.setIsLiked(feedMessage.getIsLiked());
        dto.setHasAccess(feedMessage.getIsAccess() || userId.equals(feedMessage.getUserId()));
        dto.setHasExpired(feedMessage.getCreatedAt() > System.currentTimeMillis());
        dto.setHasPaid(feedMessage.getPriceInCents() > 0);
        dto.setHasAnswer(answer.hasAnswer());
        dto.setVisibleMembershipIds(feedMessage.getMembershipIdList());
        dto.setCommentsCount(feedMessage.getCommentsCount());
        dto.setLikesCount(feedMessage.getLikesCount());
        dto.setPriceInCents(feedMessage.getPriceInCents());
        dto.setCreatedAt(feedMessage.getCreatedAt());

        if (answer.hasAnswer()) {
            dto.setAnswer(answer.getAnswer().getValue());
            dto.setAnswerer(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(answer.getAnswerUserId().getValue())));
            dto.setAnsweredAt(answer.getAnsweredAt().getValue());
            dto.setPublicVisible(!answer.hasPublicVisible() || answer.getPublicVisible().getValue());
        }

        dto.setComments(feedMessage.getLastCommentsList()
                .stream()
                .map(this.commentDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        return dto;
    }

}
