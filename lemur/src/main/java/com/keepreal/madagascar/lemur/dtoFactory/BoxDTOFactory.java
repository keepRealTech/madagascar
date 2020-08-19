package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.QuestionMessage;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.service.SubscribeMembershipService;
import org.springframework.stereotype.Component;
import swagger.model.BoxAccessDTO;
import swagger.model.BoxDTO;
import swagger.model.QuestionDTO;

import java.util.Collections;
import java.util.List;

@Component
public class BoxDTOFactory {

    private final MembershipDTOFactory membershipDTOFactory;
    private final MembershipService membershipService;
    private final SubscribeMembershipService subscribeMembershipService;

    public BoxDTOFactory(MembershipDTOFactory membershipDTOFactory,
                         MembershipService membershipService,
                         SubscribeMembershipService subscribeMembershipService) {
        this.membershipDTOFactory = membershipDTOFactory;
        this.membershipService = membershipService;
        this.subscribeMembershipService = subscribeMembershipService;
    }


    public BoxDTO boxDTO(String islandId, String userId) {
        BoxDTO dto = new BoxDTO();

        dto.setIslandId(islandId);
        dto.setAnsweredQuestionsCount(0);

        List<String> myMembershipIds = subscribeMembershipService.retrieveSubscribedMembershipsByIslandIdAndUserId(islandId, userId);

        dto.setHasSubmitAccess(false); // 通过比较user的membership确定是否可以提问

        dto.setBoxAccess(this.boxAccessDTO(islandId));
        dto.setRecentAnsweredQuestions(Collections.emptyList());

        return dto;
    }

    public BoxAccessDTO boxAccessDTO(String islandId) {
        BoxAccessDTO dto = new BoxAccessDTO();

        dto.setEnabled(false);
        dto.setMembershipIds(Collections.emptyList());

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
}
