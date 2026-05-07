package com.chat.chart.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {

    private String conversationId;

    private LocalDateTime createdAt;

    private int messageCount;

    private boolean active;
}
