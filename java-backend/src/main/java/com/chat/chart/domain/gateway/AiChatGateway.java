package com.chat.chart.domain.gateway;

/**
 * AI聊天网关接口
 * <p>
 * 定义与AI模型进行流式对话的抽象能力，
 * 具体实现可对接不同的大模型服务。
 * </p>
 *
 * @author Chat Chart System
 */
public interface AiChatGateway {

    /**
     * AI流式聊天
     * <p>
     * 将用户消息发送给AI模型，通过回调接口传递流式响应。
     * </p>
     *
     * @param message   用户消息内容
     * @param sessionId 会话ID，用于上下文关联
     * @param callback  流式事件回调
     */
    void chatStream(String message, String sessionId, AiStreamCallback callback);
}
