package com.keepreal.madagascar.lemur.dtoFactory;

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


    public BoxDTO boxDTO(BoxMessage boxMessage, String userId) {
        BoxDTO dto = new BoxDTO();

        dto.setIslandId(boxMessage.getIsland());
        dto.setAnsweredQuestionsCount(boxMessage.getAnsweredQuestionCount());

        List<String> myMembershipIds = subscribeMembershipService.retrieveSubscribedMembershipsByIslandIdAndUserId(boxMessage.getIsland(), userId);
        ProtocolStringList membershipIdsList = boxMessage.getMembershipIdsList();
        dto.setHasSubmitAccess(membershipIdsList.stream().anyMatch(myMembershipIds::contains));

        return dto;
    }

    public BoxAccessDTO boxAccessDTO(BoxMessage boxMessage) {
        BoxAccessDTO dto = new BoxAccessDTO();

        dto.setEnabled(boxMessage.getEnabled());
        dto.setMembershipIds(boxMessage.getMembershipIdsList());

        return dto;
    }

    public QuestionDTO questionDTO(FeedMessage feedMessage) {

        QuestionMessage question = feedMessage.getQuestion();

        QuestionDTO dto = new QuestionDTO();

        dto.setId(feedMessage.getId());
        dto.setIslandId(feedMessage.getIslandId());
        dto.setText(question.getText());
        dto.setHasAccess(feedMessage.getIsAccess());
        dto.setHasExpired(feedMessage.getCreatedAt() > System.currentTimeMillis()); // 根据createdAt和当前时间判断
        dto.setHasPaid(question.hasPriceInCents()); // 根据priceInCents是否不为null && 大于0
        dto.setPublicVisible(question.getPublicVisible().getValue()); //
        dto.setHasAnswer(question.hasAnswer());
        dto.setVisibleMembershipIds(feedMessage.getMembershipIdList());
        dto.setCommentsCount(feedMessage.getCommentsCount());
        dto.setLikesCount(feedMessage.getLikesCount());
        if (dto.getHasPaid()) {
            dto.setPriceInCents(question.getPriceInCents().getValue());
        }
        dto.setCreatedAt(feedMessage.getCreatedAt());

        return dto;
    }

    public FullQuestionDTO fullQuestionDTO(FeedMessage feedMessage) {

        QuestionMessage question = feedMessage.getQuestion();

        FullQuestionDTO dto = new FullQuestionDTO();
        dto.setId(feedMessage.getId());
        dto.setIslandId(feedMessage.getIslandId());
        dto.setText(question.getText());
        dto.setHasAccess(feedMessage.getIsAccess());
        dto.setHasExpired(feedMessage.getCreatedAt() > System.currentTimeMillis()); // 根据createdAt和当前时间判断
        dto.setHasPaid(question.hasPriceInCents()); // 根据priceInCents是否不为null && 大于0
        dto.setPublicVisible(question.getPublicVisible().getValue()); //
        dto.setHasAnswer(question.hasAnswer());
        dto.setVisibleMembershipIds(feedMessage.getMembershipIdList());
        dto.setCommentsCount(feedMessage.getCommentsCount());
        dto.setLikesCount(feedMessage.getLikesCount());
        if (dto.getHasPaid()) {
            dto.setPriceInCents(question.getPriceInCents().getValue());
        }
        dto.setCreatedAt(feedMessage.getCreatedAt());

        dto.setAnaswer(this.answerDTO(question));
        dto.setComments(feedMessage.getLastCommentsList()
                .stream()
                .map(this.commentDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        return dto;
    }

    public AnswerDTO answerDTO(QuestionMessage questionMessage) {
        AnswerDTO dto = new AnswerDTO();
        dto.setText(questionMessage.getAnswer().getValue());
        dto.setUser(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(questionMessage.getAnswerUserId())));
        dto.setAnsweredAt(questionMessage.getAnsweredAt());

        return dto;
    }
}
