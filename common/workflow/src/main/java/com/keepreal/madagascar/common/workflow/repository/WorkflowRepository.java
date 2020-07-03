package com.keepreal.madagascar.common.workflow.repository;

import com.keepreal.madagascar.common.workflow.model.WorkflowLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Represents the workflow repository.
 */
@Repository
public interface WorkflowRepository extends MongoRepository<WorkflowLog, String> {
}
