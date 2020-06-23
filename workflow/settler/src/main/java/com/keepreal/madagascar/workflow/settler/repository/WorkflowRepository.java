package com.keepreal.madagascar.workflow.settler.repository;

import com.keepreal.madagascar.workflow.settler.model.WorkflowLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Represents the workflow repository.
 */
@Repository
public interface WorkflowRepository extends MongoRepository<WorkflowLog, String> {
}
