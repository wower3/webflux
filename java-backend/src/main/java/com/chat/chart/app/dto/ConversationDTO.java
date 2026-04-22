package com.chat.chart.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话数据传输对象
 * <p>
 * 用于向前端返回会话摘要信息，包含会话ID、创建时间、消息数量和活跃状态。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {

    /**
     * 会话唯一标识
     */
    private String conversationId;

    /**
     * 会话创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 会话中的消息总数
     */
    private int messageCount;

    /**
     * 会话是否活跃
     */
    private boolean active;
}
