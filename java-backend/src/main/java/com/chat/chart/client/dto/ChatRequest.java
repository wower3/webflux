package com.chat.chart.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 聊天请求DTO
 */
@Data
public class ChatRequest {

    @JsonProperty("message")
    private String message;

    @JsonProperty("conversationId")
    private String conversationId;
}
