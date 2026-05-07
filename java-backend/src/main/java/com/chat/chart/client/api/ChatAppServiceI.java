package com.chat.chart.client.api;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天应用服务接口
 */
public interface ChatAppServiceI {

    /**
     * 处理聊天消息（流式）
     *
     * @param message        用户发送的消息内容
     * @param userId         当前用户ID
     * @param conversationId 会话ID（可为null，为null时自动创建新会话）
     * @return SseEmitter，用于流式推送AI响应
     */
    SseEmitter handleMessage(String message, Long userId, String conversationId);
}
