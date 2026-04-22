package com.chat.chart.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话领域模型
 * <p>
 * 表示一个完整的对话会话，归属于某个用户。
 * 一个会话可包含多个会话轮次（session）和多条消息。
 * </p>
 *
 * @author Chat Chart System
 */
@Data
public class Conversation {

    /** 会话唯一标识 */
    private String conversationId;

    /** 所属用户ID */
    private Long userId;

    /** 会话创建时间 */
    private LocalDateTime createdAt;
}
