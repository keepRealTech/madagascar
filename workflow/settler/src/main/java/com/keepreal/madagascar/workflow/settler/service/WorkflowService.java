package com.keepreal.madagascar.workflow.settler.service;

import com.keepreal.madagascar.workflow.settler.model.WorkflowLog;
import com.keepreal.madagascar.workflow.settler.repository.WorkflowRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Represents the workflow service.
 */
@Service
public class WorkflowService {

    private static final String WORKFLOW_TYPE = "settler-daily";
    private final WorkflowRepository workflowRepository;

    /**
     * Constructs the workflow service.
     *
     * @param workflowRepository {@link WorkflowRepository}.
     */
    public WorkflowService(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    /**
     * Initializes a workflow log.
     *
     * @return {@link WorkflowLog}.
     */
    public WorkflowLog initialize() {
        WorkflowLog workflowLog = WorkflowLog.builder()
                .type(WorkflowService.WORKFLOW_TYPE)
                .startTimestamp(Instant.now().toEpochMilli())
                .state("In progress")
                .build();
        return this.workflowRepository.insert(workflowLog);
    }

    /**
     * Finishes the workflow with succeed state.
     *
     * @param workflowLog {@link WorkflowLog}.
     */
    public void succeed(WorkflowLog workflowLog) {
        workflowLog.setState("Succeed");
        workflowLog.setFinishTimestamp(Instant.now().toEpochMilli());
        this.workflowRepository.save(workflowLog);
    }

    /**
     * Finishes the workflow with failed state.
     *
     * @param workflowLog {@link WorkflowLog}.
     */
    public void failed(WorkflowLog workflowLog, Throwable throwable) {
        workflowLog.setDescription(throwable.getMessage());
        workflowLog.setState("Failed");
        workflowLog.setFinishTimestamp(Instant.now().toEpochMilli());
        this.workflowRepository.save(workflowLog);
    }

}
