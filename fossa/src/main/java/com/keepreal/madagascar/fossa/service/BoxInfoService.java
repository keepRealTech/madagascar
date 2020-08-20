package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.BoxMessage;
import com.keepreal.madagascar.fossa.dao.BoxInfoRepository;
import com.keepreal.madagascar.fossa.model.BoxInfo;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.model.QuestionInfo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class BoxInfoService {

    private final BoxInfoRepository boxInfoRepository;
    private final LongIdGenerator idGenerator;

    public BoxInfoService(BoxInfoRepository boxInfoRepository,
                          LongIdGenerator idGenerator) {
        this.boxInfoRepository = boxInfoRepository;
        this.idGenerator = idGenerator;
    }

    public BoxInfo createOrUpdate(BoxInfo boxInfo) {
        boxInfo.setId(String.valueOf(idGenerator.nextId()));

        return boxInfoRepository.save(boxInfo);
    }

    public Query retrieveAnswerAndVisibleQuestion(String islandId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("islandId").is(islandId));
        query.addCriteria(Criteria.where("mediaInfos.publicVisible").is(true));
        query.addCriteria(Criteria.where("mediaInfos.answer").ne(null));

        return query;
    }

    public Query retrieveAnswerMeQuestion(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("mediaInfos.answer").ne(null));

        return query;
    }

    public Query retrieveQuestionByCondition(String userId, boolean answered, boolean paid, String membershipId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("hostId").is(userId));
        if (answered) {
            query.addCriteria(Criteria.where("mediaInfos.answer").ne(null));
            if (paid) {
                query.addCriteria(Criteria.where("mediaInfos.priceInCents").gt(0));
            }
            if (membershipId != null) {
                query.addCriteria(Criteria.where("membershipIds").is(membershipId));
            }
        } else {
            query.addCriteria(Criteria.where("mediaInfos.answer").is(null));
        }

        return query;
    }

    public BoxMessage getBoxMessage(BoxInfo boxInfo) {
        if (boxInfo == null) {
            return null;
        }

        return BoxMessage.newBuilder()
                .setId(boxInfo.getId())
                .setIsland(boxInfo.getIslandId())
                .setEnabled(boxInfo.isEnabled())
                .addAllMembershipIds(Arrays.asList(boxInfo.getMembershipIds().split(",")))
                .build();
    }
}
