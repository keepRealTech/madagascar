package com.keepreal.madagascar.lemur.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TranscodeComplete {
    @JsonProperty("EventTime")
    private String EventTime;
    @JsonProperty("EventType")
    private String EventType;
    @JsonProperty("VideoId")
    private String VideoId;
    @JsonProperty("Status")
    private String Status;
    @JsonProperty("Extend")
    private String Extend;
    @JsonProperty("StreamInfos")
    private List<StreamInfo> StreamInfos;


}
