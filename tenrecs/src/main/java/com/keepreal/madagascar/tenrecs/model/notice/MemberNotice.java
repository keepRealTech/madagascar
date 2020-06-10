package com.keepreal.madagascar.tenrecs.model.notice;

import lombok.Builder;
import lombok.Data;

/**
 * Represents the new member notice.
 */
@Builder
@Data
public class MemberNotice {

    private String islandId;
    private String memberId;
    private String membershipId;
    private String membershipName;
    private Long pricePerMonthInCents;

}