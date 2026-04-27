package com.chat.chart.infrastructure.gateway;

import com.chat.chart.app.dto.StreamEvent;
import com.chat.chart.domain.gateway.AiStreamCallback;
import com.chat.chart.domain.gateway.MockAiChatGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * Mock AI流式网关实现
 * <p>
 * 返回预设的模拟AI流式响应，包含文本内容和图表JSON示例。
 * 用于前端开发和调试阶段，无需连接真实AI服务。
 * </p>
 *
 * @author Chat Chart System
 */
@Repository
public class MockAiChatGatewayImpl implements MockAiChatGateway {

    private static final Logger log = LoggerFactory.getLogger(MockAiChatGatewayImpl.class);

    private final ObjectMapper objectMapper;

    public MockAiChatGatewayImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Mock AI流式聊天
     * <p>
     * 依次发送模拟内容片段，每段之间带有延时，
     * 模拟真实流式输出的打字机效果。
     * </p>
     *
     * @param message  用户消息内容（Mock模式下不实际使用）
     * @param callback 流式事件回调
     */
    @Override
    public void mockChatStream(String message, AiStreamCallback callback) {
        log.info("[Mock AI] 收到消息: {}", message);

        try {
            Thread.sleep(300);

            // 文本内容：开场白
            send(callback, "好的，我正在为您分析数据...\n\n", 100);
            send(callback, "以下是本月的**销售趋势分析**：\n\n", 150);

            // 折线图JSON（分片发送，模拟跨事件JSON重组场景）
            send(callback, "{\"type\":\"chart\",\"chartId\":\"chart_sales\",\"subtype\":\"line\",", 100);
            send(callback, "\"title\":\"月度销售趋势\",\"data\":{", 100);
            send(callback, "\"1月\":320,\"2月\":450,\"3月\":380,", 100);
            send(callback, "\"4月\":510,\"5月\":620,\"6月\":580", 100);
            send(callback, "}}", 200);

            // 文本内容：趋势分析
            send(callback, "\n\n从趋势图来看，**5月**达到销售峰值（620），整体呈上升趋势。\n\n", 150);

            // 柱状图JSON（分片发送）
            send(callback, "{\"type\":\"chart\",\"chartId\":\"chart_dept\",\"subtype\":\"bar\",", 100);
            send(callback, "\"title\":\"各部门业绩对比\",\"data\":{", 100);
            send(callback, "\"销售部\":850,\"技术部\":620,", 100);
            send(callback, "\"市场部\":580,\"人事部\":200", 100);
            send(callback, "}}", 200);

            // 文本内容：总结
            send(callback, "\n\n**销售部**业绩最为突出，达到了 850。如果您需要进一步的数据分析，请随时告诉我！", 150);

            // 结束事件
            sendEnd(callback);
            callback.complete();
        } catch (Exception e) {
            log.error("[Mock AI] 发送模拟响应失败", e);
            callback.error(e);
        }
    }

    /**
     * 发送带延时的SSE内容事件
     *
     * @param callback 流式事件回调
     * @param content  文本内容
     * @param delayMs  延时毫秒数
     */
    private void send(AiStreamCallback callback, String content, long delayMs) {
        try {
            Thread.sleep(delayMs);
            StreamEvent event = StreamEvent.content(content);
            callback.sendEvent(objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("[Mock AI] 发送事件失败", e);
        }
    }

    /**
     * 发送SSE结束事件
     *
     * @param callback 流式事件回调
     */
    private void sendEnd(AiStreamCallback callback) {
        try {
            StreamEvent event = StreamEvent.end();
            callback.sendEvent(objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("[Mock AI] 发送结束事件失败", e);
        }
    }
}
