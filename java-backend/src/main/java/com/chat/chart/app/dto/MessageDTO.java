package com.chat.chart.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    private String requestId;

    private String conversationId;

    private String role;

    private String content;

    private LocalDateTime createdAt;

    public static MessageDTO from(com.chat.chart.domain.model.ChatMessage msg) {
        MessageDTO dto = new MessageDTO();
        dto.setRequestId(msg.getRequestId());
        dto.setConversationId(msg.getConversationId());
        dto.setRole(msg.getRole());
        dto.setContent(msg.getContent());
        dto.setCreatedAt(msg.getCreatedAt());
        return dto;
    }
}
