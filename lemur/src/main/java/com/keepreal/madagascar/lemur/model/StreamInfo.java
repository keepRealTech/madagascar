package com.keepreal.madagascar.lemur.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class StreamInfo {
    @JsonProperty("Status")
    private String Status;
    @JsonProperty("Bitrate")
    private int Bitrate;
    @JsonProperty("Definition")
    private String Definition;
    @JsonProperty("Duration")
    private int Duration;
    @JsonProperty("Encrypt")
    private boolean Encrypt;
    @JsonProperty("FileUrl")
    private String FileUrl;
    @JsonProperty("Format")
    private String Format;
    @JsonProperty("Fps")
    private int Fps;
    @JsonProperty("Height")
    private int Height;
    @JsonProperty("Size")
    private long Size;
    @JsonProperty("Width")
    private int Width;
    @JsonProperty("JobId")
    private String JobId;
}
