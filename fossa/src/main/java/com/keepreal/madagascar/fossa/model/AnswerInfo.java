package com.keepreal.madagascar.fossa.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents the question info entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnswerInfo extends MediaInfo {

    private String answer;
    private Boolean publicVisible;
    private String answerUserId;
    private Long answeredAt;
    private Boolean ignored;

}