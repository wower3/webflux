package com.chat.chart.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 采纳状态请求DTO
 */
@Data
public class AdoptionRequest {

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("adoptionStatus")
    private String adoptionStatus;
}
