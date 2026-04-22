package com.chat.chart.domain.gateway;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

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
     * 将用户消息发送给AI模型，返回SSE格式的流式响应。
     * </p>
     *
     * @param message   用户消息内容
     * @param sessionId 会话ID，用于上下文关联
     * @return SSE事件流，包含AI的流式回复内容
     */
    Flux<ServerSentEvent<String>> chatStream(String message, String sessionId);
}
