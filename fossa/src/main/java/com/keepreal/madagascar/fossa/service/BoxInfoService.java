package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.MediaType;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.BoxMessage;
import com.keepreal.madagascar.fossa.dao.BoxInfoRepository;
import com.keepreal.madagascar.fossa.model.BoxInfo;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;

@Service
public class BoxInfoService {

    private final BoxInfoRepository boxInfoRepository;
    private final LongIdGenerator idGenerator;
    private final MongoTemplate mongoTemplate;

    public BoxInfoService(BoxInfoRepository boxInfoRepository,
                          LongIdGenerator idGenerator,
                          MongoTemplate mongoTemplate) {
        this.boxInfoRepository = boxInfoRepository;
        this.idGenerator = idGenerator;
        this.mongoTemplate = mongoTemplate;
    }

    public BoxInfo createOrUpdate(BoxInfo boxInfo) {
        if (boxInfo.getId() == null) {
            boxInfo.setId(String.valueOf(idGenerator.nextId()));
        }

        return boxInfoRepository.save(boxInfo);
    }

    public void addAnsweredQuestionCount(String islandId, String hostId) {
        BoxInfo boxInfo = this.boxInfoRepository.findBoxInfoByIslandId(islandId);
        if (boxInfo == null) {
            boxInfo = new BoxInfo();
            boxInfo.setId(String.valueOf(idGenerator.nextId()));
            boxInfo.setIslandId(islandId);
            boxInfo.setEnabled(true);
            boxInfo.setMembershipIds("");
            boxInfo.setAnsweredQuestionCount(0);
            boxInfo.setHostId(hostId);
        }
        boxInfo.setAnsweredQuestionCount(boxInfo.getAnsweredQuestionCount() + 1);
        this.boxInfoRepository.save(boxInfo);
    }

    public BoxInfo getBoxInfoByIslandId(String islandId) {
        return boxInfoRepository.findBoxInfoByIslandId(islandId);
    }

    public Query retrieveAnswerAndVisibleQuestion(String islandId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("islandId").is(islandId));
        query.addCriteria(Criteria.where("deleted").is(false));
        query.addCriteria(Criteria.where("mediaInfos.publicVisible").is(true));
        query.addCriteria(new Criteria().andOperator(Criteria.where("mediaInfos").size(1), Criteria.where("mediaInfos.0.ignored").is(false)));

        return query.with(Sort.by(Sort.Order.desc("mediaInfos.0.answeredAt"), Sort.Order.desc("createdTime")));
    }

    public Query retrieveAnswerMeQuestion(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(new Criteria().andOperator(Criteria.where("mediaInfos").size(1), Criteria.where("mediaInfos.0.ignored").is(false)));

        return query.with(Sort.by(Sort.Order.desc("mediaInfos.0.answeredAt"), Sort.Order.desc("createdTime")));
    }

    public Query retrieveQuestionByCondition(String userId, Boolean answered, Boolean paid, Boolean hasMembership) {
        Query query = new Query();
        query.addCriteria(Criteria.where("hostId").is(userId));
        query.addCriteria(Criteria.where("multiMediaType").is(MediaType.MEDIA_QUESTION.name()));
        query.addCriteria(Criteria.where("mediaInfos.ignored").ne(true));
        query.addCriteria(Criteria.where("deleted").is(false));

        if (answered != null) {
            if (answered) {
                query.addCriteria(new Criteria().andOperator(Criteria.where("mediaInfos").size(1), Criteria.where("mediaInfos.0.ignored").is(false)));
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

        if (hasMembership != null) {
            if (hasMembership) {
                query.addCriteria(new Criteria().andOperator(Criteria.where("userMembershipIds").ne(null), Criteria.where("userMembershipIds").not().size(0)));
            } else {
                query.addCriteria(new Criteria().orOperator(Criteria.where("userMembershipIds").is(null), Criteria.where("userMembershipIds").size(0)));
            }
        }

        return query.with(Sort.by(Sort.Order.desc("mediaInfos.0.answeredAt"), Sort.Order.desc("createdTime")));
    }

    public BoxMessage getBoxMessage(BoxInfo boxInfo) {
        if (boxInfo == null) {
            return null;
        }

        long totalCount = mongoTemplate.count(this.retrieveAnswerAndVisibleQuestion(boxInfo.getIslandId()), FeedInfo.class);

        return BoxMessage.newBuilder()
                .setId(boxInfo.getId())
                .setIsland(boxInfo.getIslandId())
                .setEnabled(boxInfo.isEnabled())
                .addAllMembershipIds(StringUtils.isEmpty(boxInfo.getMembershipIds()) ?
                        Collections.emptyList() :
                        Arrays.asList(boxInfo.getMembershipIds().split(",")))
                .setAnsweredQuestionCount((int) totalCount)
                .setHostId(boxInfo.getHostId())
                .build();
    }
}
