package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.BoxMessage;
import com.keepreal.madagascar.fossa.dao.BoxInfoRepository;
import com.keepreal.madagascar.fossa.model.BoxInfo;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;

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
        if (boxInfo.getId() == null) {
            boxInfo.setId(String.valueOf(idGenerator.nextId()));
        }

        return boxInfoRepository.save(boxInfo);
    }

    public void addAnsweredQuestionCount(String islandId) {
        BoxInfo boxInfo = this.boxInfoRepository.findBoxInfoByIslandId(islandId);
        boxInfo.setAnsweredQuestionCount(boxInfo.getAnsweredQuestionCount() + 1);
        this.boxInfoRepository.save(boxInfo);
    }

    public BoxInfo getBoxInfoByIslandId(String islandId) {
        return boxInfoRepository.findBoxInfoByIslandId(islandId);
    }

    public Query retrieveAnswerAndVisibleQuestion(String islandId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("islandId").is(islandId));
        query.addCriteria(Criteria.where("mediaInfos.publicVisible").is(true));
        query.addCriteria(Criteria.where("mediaInfos").size(1));

        return query;
    }

    public Query retrieveAnswerMeQuestion(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("mediaInfos").size(1));

        return query;
    }

    public Query retrieveQuestionByCondition(String userId, Boolean answered, Boolean paid, String membershipId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("hostId").is(userId));

        if (answered != null) {
            if (answered) {
                query.addCriteria(Criteria.where("mediaInfos").size(1));
            } else {
                query.addCriteria(Criteria.where("mediaInfos").size(0));
            }
        }

        if (paid != null) {
            if (paid) {
                query.addCriteria(Criteria.where("priceInCents").gt(0));
            } else {
                query.addCriteria(new Criteria().orOperator(Criteria.where("priceInCents").is(0), Criteria.where("priceInCents").is(null)));
            }
        }

        if (!StringUtils.isEmpty(membershipId)) {
            query.addCriteria(Criteria.where("membershipIds").is(membershipId));
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
                .addAllMembershipIds(StringUtils.isEmpty(boxInfo.getMembershipIds()) ?
                        Collections.emptyList() :
                        Arrays.asList(boxInfo.getMembershipIds().split(",")))
                .setAnsweredQuestionCount(boxInfo.getAnsweredQuestionCount())
                .setHostId(boxInfo.getHostId())
                .build();
    }
}
