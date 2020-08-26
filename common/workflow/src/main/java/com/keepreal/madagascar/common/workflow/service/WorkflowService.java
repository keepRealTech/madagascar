package com.keepreal.madagascar.common.workflow.service;

import com.keepreal.madagascar.common.workflow.config.WorkflowConfiguration;
import com.keepreal.madagascar.common.workflow.model.WorkflowLog;
import com.keepreal.madagascar.common.workflow.repository.WorkflowRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Represents the workflow service.
 */
@Service
public class WorkflowService {

    private final WorkflowConfiguration workflowConfiguration;
    private final WorkflowRepository workflowRepository;

    /**
     * Constructs the workflow service.
     *
     * @param workflowConfiguration {@link WorkflowConfiguration}.
     * @param workflowRepository    {@link WorkflowRepository}.
     */
    public WorkflowService(WorkflowConfiguration workflowConfiguration,
                           WorkflowRepository workflowRepository) {
        this.workflowConfiguration = workflowConfiguration;
        this.workflowRepository = workflowRepository;
    }

    /**
     * Initializes a workflow log.
     *
     * @param label Label.
     * @return {@link WorkflowLog}.
     */
    public WorkflowLog initialize(String label) {
        WorkflowLog workflowLog = WorkflowLog.builder()
                .type(this.workflowConfiguration.getType() + label)
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

    /**
     * Finishes the workflow with failed state.
     *
     * @param workflowLog {@link WorkflowLog}.
     */
    public void failed(WorkflowLog workflowLog, String message) {
        workflowLog.setDescription(message);
        workflowLog.setState("Failed");
        workflowLog.setFinishTimestamp(Instant.now().toEpochMilli());
        this.workflowRepository.save(workflowLog);
    }

}
