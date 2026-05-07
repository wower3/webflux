package com.chat.chart.client.dto;

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
}
