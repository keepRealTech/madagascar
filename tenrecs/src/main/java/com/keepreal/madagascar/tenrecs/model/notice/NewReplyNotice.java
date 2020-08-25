package com.keepreal.madagascar.tenrecs.model.notice;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NewReplyNotice {
    private String feedId;
    private String authorId;
}
