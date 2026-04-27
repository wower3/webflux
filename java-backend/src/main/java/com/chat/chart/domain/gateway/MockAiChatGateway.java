package com.chat.chart.domain.gateway;

/**
 * Mock AI聊天网关接口
 * <p>
 * 提供模拟的AI流式响应能力，用于开发和调试阶段，
 * 无需实际调用外部AI服务。
 * </p>
 *
 * @author Chat Chart System
 */
public interface MockAiChatGateway {

    /**
     * Mock AI流式聊天
     * <p>
     * 返回预设的模拟响应数据，包含文本内容和图表JSON示例。
     * 通过回调接口将事件数据传递给调用方。
     * </p>
     *
     * @param message  用户消息内容（Mock模式下不实际使用）
     * @param callback 流式事件回调
     */
    void mockChatStream(String message, AiStreamCallback callback);
}
