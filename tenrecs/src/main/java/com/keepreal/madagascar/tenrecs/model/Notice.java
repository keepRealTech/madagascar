package com.keepreal.madagascar.tenrecs.model;

import com.keepreal.madagascar.common.NoticeType;
import com.keepreal.madagascar.tenrecs.model.notice.MemberNotice;
import com.keepreal.madagascar.tenrecs.model.notice.SubscribeNotice;
import lombok.Builder;
import lombok.Data;

/**
 * Represents the notice.
 */
@Builder
@Data
public class Notice {

    private NoticeType type;
    private SubscribeNotice subscribeNotice;
    private MemberNotice memberNotice;

}
