package com.chat.chart.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 聊天请求模型
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
@Data
public class ChatRequest {

    /**
     * 用户消息内容
     */
    @JsonProperty("message")
    private String message;

    /**
     * 会话ID（可选）
     */
    @JsonProperty("session_id")
    private String sessionId;
}
