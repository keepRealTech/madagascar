package com.keepreal.madagascar.fossa.model;

import lombok.Data;

/**
 * Represents the question info entity.
 */
@Data
public class QuestionInfo extends MediaInfo {

    private String answer;
    private Long priceInCents;
    private Boolean publicVisible;
    private String answerUserId;
    private Long answerAt;

}