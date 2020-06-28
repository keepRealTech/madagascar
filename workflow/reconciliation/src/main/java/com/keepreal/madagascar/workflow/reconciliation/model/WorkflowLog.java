package com.keepreal.madagascar.workflow.reconciliation.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.EntityListeners;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the workflow log.
 */
@Data
@Builder
@Document(value = "workflowLog")
@EntityListeners(AuditingEntityListener.class)
public class WorkflowLog {

    @Id
    private String id;
    private String type;

    @Builder.Default
    private List<String> paymentIds = new ArrayList<>();
    @Builder.Default
    private List<ReconciliationInfo> reconciliationInfos = new ArrayList<>();

    private Long startTimestamp;
    private Long finishTimestamp;

    private String state;
    private String description;

    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;

}
