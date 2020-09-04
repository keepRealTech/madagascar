package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.dao.MergeUserLogRepository;
import com.keepreal.madagascar.coua.model.MergeUserLog;
import org.springframework.stereotype.Service;

@Service
public class MergeUserLogService {

    private final MergeUserLogRepository mergeUserLogRepository;
    private final LongIdGenerator idGenerator;

    public MergeUserLogService(MergeUserLogRepository mergeUserLogRepository,
                               LongIdGenerator idGenerator) {
        this.mergeUserLogRepository = mergeUserLogRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * 创建合并用户log
     *
     * @param mergeUserLog {@link MergeUserLog}
     */
    public void createNewMergeUserLog(MergeUserLog mergeUserLog) {
        mergeUserLog.setId(String.valueOf(idGenerator.nextId()));
        this.mergeUserLogRepository.save(mergeUserLog);
    }

}
