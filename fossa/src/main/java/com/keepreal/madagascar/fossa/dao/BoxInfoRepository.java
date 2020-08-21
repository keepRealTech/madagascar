package com.keepreal.madagascar.fossa.dao;

import com.keepreal.madagascar.fossa.model.BoxInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoxInfoRepository extends JpaRepository<BoxInfo, String> {

    BoxInfo findBoxInfoByIslandId(String islandId);
}
