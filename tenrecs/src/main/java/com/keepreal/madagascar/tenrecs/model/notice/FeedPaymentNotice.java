package com.keepreal.madagascar.tenrecs.model.notice;

import lombok.Builder;
import lombok.Data;

/**
 * Represents the new feed payment notice.
 */
@Builder
@Data
public class FeedPaymentNotice {

    private String feedId;
    private String userId;
    private Long priceInCents;

}
