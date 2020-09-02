package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.MergeUserLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MergeUserLogRepository extends JpaRepository<MergeUserLog, String> {

}
