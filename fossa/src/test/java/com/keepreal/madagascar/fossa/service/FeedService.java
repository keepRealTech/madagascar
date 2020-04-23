package com.keepreal.madagascar.fossa.service;


import com.keepreal.madagascar.fossa.FossaApplication;
import com.keepreal.madagascar.fossa.dao.FeedInfoRepository;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.model.UserInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-23
 **/

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FossaApplication.class)
public class FeedService {

    @Autowired
    private FeedInfoRepository feedInfoRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testSave() {
        FeedInfo feedInfo = new FeedInfo();
        feedInfo.setIslandId("qazwsx");
        feedInfo.setContent("123456qwerty");
        UserInfo userInfo = new UserInfo();
        userInfo.setName("123");
        userInfo.setImageUrl("789");
//        feedInfo.setUserInfo(Collections.singletonList(userInfo));
        feedInfo.setImageUrls(Arrays.asList("1.jpg", "2.jpg", "3.jpg"));
        feedInfo.setLikesCount(50);
        feedInfo.setCommentsCount(60);
        feedInfo.setRepostCount(70);
        feedInfo.setState(1);
        feedInfoRepository.save(feedInfo);
    }

    @Test
    public void testGet() {
        Document condition = new Document();
        condition.put("_id", "5ea1673e2fa10622c4a84151");
        Document fields = new Document();
        fields.put("content", true);
        Query query = new BasicQuery(condition, fields);
        Query q = new Query();
        q.addCriteria(Criteria.where("id").is("5ea1673e2fa10622c4a84151"));
        q.fields().include("imageUrls");
        FeedInfo urls = mongoTemplate.findOne(q, FeedInfo.class);
        System.out.println(urls);
    }

    @Test
    public void testUpdate() {
        Query query = new Query(Criteria.where("id").is("5ea1673e2fa10622c4a84151"));
        Update update = new Update();
        update.push("imageUrls", "5.jpg");

        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, FeedInfo.class);
        System.out.println(updateResult);
    }

    @Test
    public void testUpdate2() {
        Query query = new Query(Criteria.where("id").is("5ea18fc42fa106240890972a").and("userInfo.$.name").is("123"));

        List<UserInfo> feedInfos = mongoTemplate.find(query, UserInfo.class);
        System.out.println(feedInfos);
    }

    @Test
    public void testUpdate3() {
        Query query = new Query(Criteria.where("id").is("5ea18fc42fa106240890972a"));
        UserInfo userInfo1 = new UserInfo("1", "qwe");
//        UserInfo userInfo2 = new UserInfo("12", "asd");
//        UserInfo userInfo3 = new UserInfo("12", "zxc");
        Update update = new Update();
        update.addToSet("userInfo").each(userInfo1);

        mongoTemplate.updateFirst(query, update, FeedInfo.class);

    }
}
