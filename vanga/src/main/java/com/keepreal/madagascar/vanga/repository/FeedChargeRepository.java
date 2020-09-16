package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.FeedCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedChargeRepository extends JpaRepository<FeedCharge, String> {

    FeedCharge findFeedChargeByUserIdAndFeedIdAndDeletedIsFalse(String userId, String feedId);
}
