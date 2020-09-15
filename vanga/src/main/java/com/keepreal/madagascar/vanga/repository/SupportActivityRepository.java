package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.SupportActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportActivityRepository extends JpaRepository<SupportActivity, String> {

    SupportActivity findSupportActivityByUserIdAndDeletedIsFalse(String userId);
}
