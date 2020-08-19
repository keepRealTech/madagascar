package com.keepreal.madagascar.lemur.dtoFactory;

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

@Component
public class BoxDTOFactory {

    private final MembershipDTOFactory membershipDTOFactory;
    private final MembershipService membershipService;
    private final SubscribeMembershipService subscribeMembershipService;
    private final UserService userService;
    private final UserDTOFactory userDTOFactory;

    public BoxDTOFactory(MembershipDTOFactory membershipDTOFactory,
                         MembershipService membershipService,
                         SubscribeMembershipService subscribeMembershipService,
                         UserService userService,
                         UserDTOFactory userDTOFactory) {
        this.membershipDTOFactory = membershipDTOFactory;
        this.membershipService = membershipService;
        this.subscribeMembershipService = subscribeMembershipService;
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
    }


    public BoxDTO boxDTO(String islandId, String userId) {
        BoxDTO dto = new BoxDTO();

        dto.setIslandId(islandId);
        dto.setAnsweredQuestionsCount(0);

        List<String> myMembershipIds = subscribeMembershipService.retrieveSubscribedMembershipsByIslandIdAndUserId(islandId, userId);

        dto.setHasSubmitAccess(false); // 通过比较user的membership确定是否可以提问

//        dto.setBoxAccess(this.boxAccessDTO(islandId));
        dto.setRecentAnsweredQuestions(Collections.emptyList());

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
        dto.setHasAccess(false);
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
        dto.setHasAccess(false);
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

        //dto.setAnaswer(this.answerDTO(question));
        return null;
    }

    public AnswerDTO answerDTO(QuestionMessage questionMessage, String userId) {
        AnswerDTO dto = new AnswerDTO();
        dto.setText(questionMessage.getAnswer().getValue());
        dto.setUser(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(userId)));

        return dto;
    }
}
