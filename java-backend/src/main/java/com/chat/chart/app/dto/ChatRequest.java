package com.chat.chart.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 聊天请求DTO
 * <p>
 * 封装前端发送的聊天请求参数，包括消息内容和会话ID。
 * </p>
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
     * 对话ID（可选，不传时自动创建新对话）
     */
    @JsonProperty("conversationId")
    private String conversationId;
}
