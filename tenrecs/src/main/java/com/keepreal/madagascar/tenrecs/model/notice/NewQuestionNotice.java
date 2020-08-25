package com.keepreal.madagascar.tenrecs.model.notice;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NewQuestionNotice {
    private String feedId;
    private String authorId;
}
