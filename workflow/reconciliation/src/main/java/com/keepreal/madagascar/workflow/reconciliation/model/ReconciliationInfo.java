package com.keepreal.madagascar.workflow.reconciliation.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReconciliationInfo {
    private String tradeNumber;
    private String type;
    private String fullInformation;
}
