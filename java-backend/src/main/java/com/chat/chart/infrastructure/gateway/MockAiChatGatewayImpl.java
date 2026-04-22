package com.chat.chart.infrastructure.gateway;

import com.chat.chart.app.dto.StreamEvent;
import com.chat.chart.domain.gateway.MockAiChatGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

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

    /** JSON序列化工具 */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Mock AI流式聊天
     * <p>
     * 返回包含销售趋势分析文本、折线图和柱状图示例数据的模拟响应。
     * 各段内容之间带有延时，模拟真实流式输出的打字机效果。
     * </p>
     *
     * @param message 用户消息内容（Mock模式下不实际使用）
     * @return SSE事件流，依次包含文本内容、图表JSON和结束事件
     */
    @Override
    public Flux<ServerSentEvent<String>> mockChatStream(String message) {
        log.info("[Mock AI] 收到消息: {}", message);

        return Flux.concat(
                // 初始延迟，模拟连接耗时
                Mono.delay(Duration.ofMillis(300)).then(Mono.empty()),

                // 文本内容：开场白
                sseContent("好的，我正在为您分析数据...\n\n", 100),
                sseContent("以下是本月的**销售趋势分析**：\n\n", 150),

                // 折线图JSON（分片发送，模拟跨事件JSON重组场景）
                sseContent("{\"type\":\"chart\",\"chartId\":\"chart_sales\",\"subtype\":\"line\",", 100),
                sseContent("\"title\":\"月度销售趋势\",\"data\":{", 100),
                sseContent("\"1月\":320,\"2月\":450,\"3月\":380,", 100),
                sseContent("\"4月\":510,\"5月\":620,\"6月\":580", 100),
                sseContent("}}", 200),

                // 文本内容：趋势分析
                sseContent("\n\n从趋势图来看，**5月**达到销售峰值（620），整体呈上升趋势。\n\n", 150),

                // 柱状图JSON（分片发送）
                sseContent("{\"type\":\"chart\",\"chartId\":\"chart_dept\",\"subtype\":\"bar\",", 100),
                sseContent("\"title\":\"各部门业绩对比\",\"data\":{", 100),
                sseContent("\"销售部\":850,\"技术部\":620,", 100),
                sseContent("\"市场部\":580,\"人事部\":200", 100),
                sseContent("}}", 200),

                // 文本内容：总结
                sseContent("\n\n**销售部**业绩最为突出，达到了 850。如果您需要进一步的数据分析，请随时告诉我！", 150),

                // 结束事件
                sseEnd()
        );
    }

    /**
     * 构建带延时的SSE内容事件
     *
     * @param content 文本内容
     * @param delayMs 延时毫秒数
     * @return SSE内容事件的Mono
     */
    private Mono<ServerSentEvent<String>> sseContent(String content, long delayMs) {
        return Mono.delay(Duration.ofMillis(delayMs))
                .then(sseContent(content));
    }

    /**
     * 构建SSE内容事件
     * <p>
     * 将文本内容包装为 {@link StreamEvent}，序列化为JSON后构建SSE事件。
     * </p>
     *
     * @param content 文本内容
     * @return SSE内容事件的Mono
     */
    private Mono<ServerSentEvent<String>> sseContent(String content) {
        return Mono.fromSupplier(() -> {
            try {
                StreamEvent event = StreamEvent.content(content);
                return ServerSentEvent.<String>builder()
                        .data(objectMapper.writeValueAsString(event))
                        .build();
            } catch (Exception e) {
                log.error("生成SSE事件失败", e);
                return ServerSentEvent.<String>builder().build();
            }
        });
    }

    /**
     * 构建SSE结束事件
     *
     * @return SSE结束事件的Mono
     */
    private Mono<ServerSentEvent<String>> sseEnd() {
        return Mono.fromSupplier(() -> {
            try {
                StreamEvent event = StreamEvent.end();
                return ServerSentEvent.<String>builder()
                        .data(objectMapper.writeValueAsString(event))
                        .build();
            } catch (Exception e) {
                log.error("生成结束事件失败", e);
                return ServerSentEvent.<String>builder().build();
            }
        });
    }
}
