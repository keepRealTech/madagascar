package com.keepreal.madagascar.angonoka.dao;

import com.keepreal.madagascar.angonoka.model.SuperFollowSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuperFollowSubscriptionRepository extends JpaRepository<SuperFollowSubscription, String> {

    List<SuperFollowSubscription> findAllByHostIdAndTypeAndDeletedIsFalse(String hostId, int type);

    SuperFollowSubscription findTopByOpenIdAndHostIdAndPlatformIdAndType(String openId, String hostId, String platformId, int type);

}
