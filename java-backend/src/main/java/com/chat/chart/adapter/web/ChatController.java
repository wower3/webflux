package com.chat.chart.adapter.web;

import com.chat.chart.app.dto.ChatRequest;
import com.chat.chart.app.service.ChatAppService;
import com.chat.chart.app.service.ChatAppService.ChatStreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 聊天控制器
 * <p>
 * 提供聊天相关的API接口，包括根路径信息、健康检查、
 * 流式聊天（SSE）和普通聊天接口。
 * 需要通过 {@link com.chat.chart.adapter.config.AuthFilter} 的token认证。
 * </p>
 *
 * @see ChatAppService
 */
@RestController
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    /** 聊天应用服务，处理消息并生成AI响应 */
    private final ChatAppService chatAppService;

    /**
     * 构造方法，注入聊天服务
     *
     * @param chatAppService 聊天应用服务
     */
    public ChatController(ChatAppService chatAppService) {
        this.chatAppService = chatAppService;
    }

    /**
     * 根路径，返回API服务基本信息
     *
     * @return 包含服务名称和版本号的Map
     */
    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Chat Chart API is running");
        result.put("version", "1.0.0");
        return result;
    }

    /**
     * 健康检查接口
     *
     * @return 包含健康状态的Map
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "healthy");
        return result;
    }

    /**
     * 流式聊天接口（SSE）
     * <p>
     * 接收用户消息，以Server-Sent Events流式返回AI响应内容，
     * 支持实时推送文本和图表数据。
     * </p>
     *
     * @param chatRequest 聊天请求体，包含消息内容和会话ID
     * @param userId      从认证过滤器传递的用户ID
     * @return SSE事件流，包含文本内容、图表数据等
     */
    @PostMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(
            @RequestBody ChatRequest chatRequest,
            @RequestAttribute("userId") Long userId) {

        log.info("[POST /api/chat/stream] message={}, userId={}", chatRequest.getMessage(), userId);

        return chatAppService.handleMessage(
                chatRequest.getMessage(),
                userId,
                chatRequest.getConversationId()
        ).flatMapMany(ChatStreamResult::getStream);
    }

    /**
     * 普通聊天接口（非流式）
     * <p>
     * 接收用户消息，等待AI完整响应后一次性返回文本内容。
     * 适用于不需要实时流式展示的场景。
     * </p>
     *
     * @param chatRequest 聊天请求体，包含消息内容和会话ID
     * @param userId      从认证过滤器传递的用户ID
     * @return 响应式包装的响应实体，包含完整消息文本
     */
    @PostMapping("/api/chat")
    public Mono<ResponseEntity<Map<String, Object>>> chat(@RequestBody ChatRequest chatRequest,
                                                           @RequestAttribute("userId") Long userId) {
        log.info("[POST /api/chat] message={}, userId={}", chatRequest.getMessage(), userId);

        StringBuilder contentBuilder = new StringBuilder();

        return chatAppService.handleMessage(
                chatRequest.getMessage(),
                userId,
                chatRequest.getConversationId()
        ).flatMapMany(result -> result.getStream()
                .doOnNext(sse -> {
                    // 从SSE事件中提取文本内容并拼接
                    if (sse.data() != null) {
                        try {
                            JsonNode node = new ObjectMapper().readTree(sse.data());
                            if ("content".equals(node.path("type").asText())) {
                                contentBuilder.append(node.path("data").asText(""));
                            }
                        } catch (Exception e) {
                            contentBuilder.append(sse.data());
                        }
                    }
                })
        ).collectList()
        .map(events -> {
            Map<String, Object> body = new HashMap<>();
            body.put("message", contentBuilder.toString());
            return ResponseEntity.ok(body);
        })
        .defaultIfEmpty(ResponseEntity.ok(Collections.<String, Object>singletonMap("message", "")));
    }
}
