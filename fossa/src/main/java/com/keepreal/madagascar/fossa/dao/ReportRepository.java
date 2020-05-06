package com.keepreal.madagascar.fossa.dao;

import com.keepreal.madagascar.fossa.model.ReportInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-29
 **/

@Repository
public interface ReportRepository extends JpaRepository<ReportInfo, Long> {
}
