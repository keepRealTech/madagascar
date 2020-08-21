package com.keepreal.madagascar.fossa.model;

import lombok.Data;

/**
 * Represents the question info entity.
 */
@Data
public class AnswerInfo extends MediaInfo {

    private String answer;
    private Boolean publicVisible;
    private String answerUserId;
    private Long answeredAt;
    private Boolean ignored;

}