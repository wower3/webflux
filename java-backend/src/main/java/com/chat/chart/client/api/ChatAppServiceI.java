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
    SseEmitter handleMessage(String message, String userId, String conversationId);

    /**
     * 更新一轮对话的采纳状态
     *
     * @param requestId      请求ID（一轮对话的标识）
     * @param adoptionStatus 采纳状态（0-未采纳，1-已采纳，2-默认）
     */
    void updateAdoptionStatus(String requestId, String adoptionStatus);
}
