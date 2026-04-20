package com.chat.chart.service;

import com.chat.chart.model.StreamEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Mock AI 流式服务
 * 返回模拟的 AI 流式响应，用于前端调试
 */
@Service
public class MockAiChatService {

    private static final Logger log = LoggerFactory.getLogger(MockAiChatService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 返回模拟的 AI 流式响应
     *
     * @param message 用户消息
     * @return SSE 事件流
     */
    public Flux<ServerSentEvent<String>> mockChatStream(String message) {
        log.info("[Mock AI] 收到消息: {}", message);

        return Flux.concat(
                Mono.delay(Duration.ofMillis(300)).then(Mono.empty()),

                sseContent("好的，我正在为您分析数据...\n\n", 100),
                sseContent("以下是本月的**销售趋势分析**：\n\n", 150),

                // 折线图
                sseContent("{\"type\":\"chart\",\"chartId\":\"chart_sales\",\"subtype\":\"line\",", 100),
                sseContent("\"title\":\"月度销售趋势\",\"data\":{", 100),
                sseContent("\"1月\":320,\"2月\":450,\"3月\":380,", 100),
                sseContent("\"4月\":510,\"5月\":620,\"6月\":580", 100),
                sseContent("}}", 200),

                sseContent("\n\n从趋势图来看，**5月**达到销售峰值（620），整体呈上升趋势。\n\n", 150),

                // 柱状图
                sseContent("{\"type\":\"chart\",\"chartId\":\"chart_dept\",\"subtype\":\"bar\",", 100),
                sseContent("\"title\":\"各部门业绩对比\",\"data\":{", 100),
                sseContent("\"销售部\":850,\"技术部\":620,", 100),
                sseContent("\"市场部\":580,\"人事部\":200", 100),
                sseContent("}}", 200),

                sseContent("\n\n**销售部**业绩最为突出，达到了 850。如果您需要进一步的数据分析，请随时告诉我！", 150),

                sseEnd()
        );
    }

    private Mono<ServerSentEvent<String>> sseContent(String content, long delayMs) {
        return Mono.delay(Duration.ofMillis(delayMs))
                .then(sseContent(content));
    }

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
