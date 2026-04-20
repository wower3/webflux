package com.chat.chart.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseAgentRequest<T> {

    @JsonProperty("appId")
    private String appId;

    @JsonProperty("trCode")
    private String trCode;

    @JsonProperty("trVersion")
    private String trVersion;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("data")
    private T data;
}
