package com.chat.chart.service;

import com.chat.chart.model.*;
import com.chat.chart.util.SymbolConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 聊天服务 - 流式响应
 * 支持SSE流式输出，图表JSON可被拆分到多个事件中
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成流式聊天响应
     * 图表JSON会被拆分到多个SSE事件中发送，模拟真实场景
     *
     * @param message 用户消息
     * @return SSE事件流
     */
    public Flux<ServerSentEvent<String>> generateChatResponse(String message) {
        log.info("[Chat] 收到消息: {}", message);

        return Flux.concat(
                // 初始延迟
                Mono.delay(Duration.ofMillis(500)).then(Mono.empty()),

                // 开场文本
                sseContent("好的，我已经根据您提供的数据为您生成了对应的图表分析。", 250),
                sseContent("\n\n", 250),
                sseContent("首先是**数量随时间变化**的折线图：\n\n", 250),

                // 折线图JSON（拆分发送）
                sseContent("{\"type\":\"chart\",\"chartId\":\"chart_time\",\"subtype\":\"line\",", 250),
                sseContent("\"title\":\"数量随时间变化\",\"data\":{", 250),
                sseContent("\"时间1\":224,\"时间2\":268,", 250),
                sseContent("\"时间3\":307,\"时间4\":221", 250),
                sseContent("}}", 500),

                // 中间文本
                sseContent("\n\n从上图中可以看出，数量在\"时间3\"达到了峰值（307），而在\"时间4\"有明显的回落。", 250),
                sseContent("\n\n", 250),
                sseContent("接下来是**本周各分类数量**的柱状图对比：\n\n", 250),

                // 柱状图JSON（拆分发送）
                sseContent("{\"type\":\"chart\",\"chartId\":\"chart_category\",\"subtype\":\"bar\",", 250),
                sseContent("\"title\":\"本周各分类数量\",\"data\":{", 250),
                sseContent("\"分类1\":127,\"分类2\":555,", 250),
                sseContent("\"分类3\":238,\"分类4\":700,", 250),
                sseContent("\"分类5\":450", 250),
                sseContent("}}", 500),

                // 结束文本
                sseContent("\n\n通过对比可知，**分类4**的数据表现最为突出，达到了 700；**分类2**紧随其后（555）；**分类5**表现中等（450）；而分类1的数据量相对最少。", 250),
                sseContent("\n\n如果您还有其他数据需要分析或调整图表格式，请随时告诉我！", 250),

                // 结束标志
                sseEnd()
        );
    }

    /**
     * 生成回显流式响应
     * 将用户输入的消息原样流式返回，用于测试前端渲染
     *
     * @param message 用户输入的消息
     * @return SSE事件流
     */
    public Flux<ServerSentEvent<String>> generateEchoStream(String message) {
        log.info("[Echo] 收到消息: 长度={}, 前50字符={}", message.length(),
                message.substring(0, Math.min(50, message.length())));

        if (message == null || message.trim().isEmpty()) {
            return Flux.concat(
                    sseContent("请输入要回显的内容"),
                    sseEnd()
            );
        }

        // 统一处理：先标准化（中文符号转英文），然后原样发送
        String normalized = SymbolConverter.normalize(message.trim());
        log.info("[Echo] 标准化后长度={}", normalized.length());

        // 使用codePoints正确处理Unicode字符（包括中文、emoji等）
        return Flux.fromStream(normalized.codePoints().mapToObj(cp -> new String(new int[]{cp}, 0, 1)))
                .delayElements(Duration.ofMillis(30))
                .flatMap(charStr -> sseContent(charStr))
                .concatWith(sseEnd());
    }

    /**
     * 从文件读取内容并流式发送
     *
     * @param filename 测试文件名
     * @param chunkSize 每个片段的字符数
     * @param delay 每个片段之间的延迟（毫秒）
     * @return SSE事件流
     */
    public Flux<ServerSentEvent<String>> generateStreamFromFile(String filename, int chunkSize, long delay) {
        try {
            Path path = Paths.get("src/main/resources", filename);
            if (!Files.exists(path)) {
                log.warn("[File] 测试文件不存在: {}", filename);
                return Flux.concat(
                        sseContent("[错误] 测试文件不存在: " + filename),
                        sseEnd()
                );
            }

            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            log.info("[File] 从文件读取内容，长度={}", content.length());

            return splitTextAndStream(content, chunkSize, Duration.ofMillis(delay))
                    .concatWith(sseEnd());

        } catch (Exception e) {
            log.error("[File] 读取文件失败", e);
            return Flux.concat(
                    sseContent("[错误] 读取文件失败: " + e.getMessage()),
                    sseEnd()
            );
        }
    }

    /**
     * 按固定长度拆分文本并流式发送
     *
     * @param content 要发送的完整内容
     * @param chunkSize 每个片段的字符数
     * @param delay 每个片段之间的延迟
     * @return SSE事件流
     */
    private Flux<ServerSentEvent<String>> splitTextAndStream(String content, int chunkSize, Duration delay) {
        return Flux.interval(delay)
                .takeWhile(index -> (int) (long) index * chunkSize < content.length())
                .map(index -> {
                    int start = (int) (long) index * chunkSize;
                    int end = Math.min(start + chunkSize, content.length());
                    return content.substring(start, end);
                })
                .flatMap(this::sseContent);
    }

    /**
     * 生成内容SSE事件
     *
     * @param content 文本内容
     * @return SSE事件
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
     * 生成带延迟的内容SSE事件
     *
     * @param content 文本内容
     * @param delayMs 延迟毫秒数
     * @return SSE事件
     */
    private Mono<ServerSentEvent<String>> sseContent(String content, long delayMs) {
        return Mono.delay(Duration.ofMillis(delayMs))
                .then(sseContent(content));
    }

    /**
     * 生成结束SSE事件
     *
     * @return SSE事件
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

    // ==================== AI服务集成预留接口 ====================

    /**
     * AI服务集成接口 - 预留
     * 后续可集成真实的AI服务（如OpenAI、Anthropic、文心一言等）
     *
     * @param message 用户消息
     * @param sessionId 会话ID
     * @return AI响应流
     */
    public Flux<ServerSentEvent<String>> callAiService(String message, String sessionId) {
        // TODO: 集成真实AI服务
        log.info("[AI] 预留接口 - message: {}, sessionId: {}", message, sessionId);

        return generateChatResponse(message);

        /*
        集成示例代码框架：

        // 1. 调用AI API
        WebClient aiClient = WebClient.create("https://api.ai-service.com");

        return aiClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer YOUR_API_KEY")
                .bodyValue(Map.of(
                    "model", "gpt-4",
                    "messages", List.of(Map.of("role", "user", "content", message)),
                    "stream", true
                ))
                .retrieve()
                .bodyToFlux(String.class)
                .flatMap(this::parseAiStreamToSSE);

        */
    }
}
