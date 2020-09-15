package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.UserQualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserQualificationRepository extends JpaRepository<UserQualification, String> {

    List<UserQualification> findUserQualificationsByUserIdAndDeletedIsFalseOrderByCreatedTime(String userId);
}
