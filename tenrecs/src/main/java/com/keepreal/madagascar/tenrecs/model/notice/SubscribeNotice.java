package com.keepreal.madagascar.tenrecs.model.notice;

import lombok.Builder;
import lombok.Data;

/**
 * Represents the subscribe notice.
 */
@Builder
@Data
public class SubscribeNotice {

    private String islandId;
    private String subscriberId;

}