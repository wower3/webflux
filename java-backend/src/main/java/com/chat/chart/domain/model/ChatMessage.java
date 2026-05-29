package com.chat.chart.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息领域模型
 * <p>
 * 表示一次对话中的单条消息，包括用户消息和AI回复。
 * 同一轮对话的 user 和 assistant 消息共享相同的 request_id。
 * </p>
 *
 * @author Chat Chart System
 */
@Data
public class ChatMessage {

    /** 所属会话ID */
    private String conversationId;

    /** 请求ID，一轮对话中 user 和 assistant 共用 */
    private String requestId;

    /** 消息角色：user（用户）/ assistant（AI助手） */
    private String role;

    /** 消息文本内容 */
    private String content;

    /** 消息创建时间 */
    private LocalDateTime createdAt;

    /** 采纳状态：0-未采纳，1-已采纳，2-默认 */
    private String adoptionStatus;

    /** 调用是否成功：1-成功，0-失败 */
    private String isSuccess;
}
