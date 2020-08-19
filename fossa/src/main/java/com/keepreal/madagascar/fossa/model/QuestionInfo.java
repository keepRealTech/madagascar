package com.keepreal.madagascar.fossa.model;

import lombok.Data;

@Data
public class QuestionInfo extends MediaInfo {

    private String text;
    private Long priceInCents;
    private String questionSkuId;
    private String receipt;
    private String transactionId;
    private String answer;
    private Boolean publicVisible;
}
