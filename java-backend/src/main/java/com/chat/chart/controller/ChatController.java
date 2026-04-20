package com.chat.chart.controller;

import com.chat.chart.model.ChatRequest;
import com.chat.chart.service.AiChatService;
import com.chat.chart.service.ChatService;
import com.chat.chart.service.MockAiChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 聊天API控制器
 * 使用函数式路由，支持SSE流式响应
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
@Component
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;
    private final AiChatService aiChatService;
    private final MockAiChatService mockAiChatService;
    private final ObjectMapper objectMapper;

    public ChatController(ChatService chatService, AiChatService aiChatService, MockAiChatService mockAiChatService) {
        this.chatService = chatService;
        this.aiChatService = aiChatService;
        this.mockAiChatService = mockAiChatService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 根路径 - 返回版本信息
     */
    public Mono<ServerResponse> root(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RootResponse("Chat Chart API is running", "1.0.0"));
    }

    /**
     * 健康检查
     */
    public Mono<ServerResponse> health(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new HealthResponse("healthy"));
    }

    /**
     * 流式聊天接口（GET）
     * 用于EventSource连接
     */
    public Mono<ServerResponse> chatStreamGet(ServerRequest request) {
        String message = request.queryParam("message").orElse("");
        boolean testMode = request.queryParam("test").map(Boolean::parseBoolean).orElse(false);

        log.info("[GET /api/chat/stream] message={}, testMode={}", message, testMode);

        Flux<ServerSentEvent<String>> stream;
        if (testMode) {
            // 测试文件模式
            stream = chatService.generateStreamFromFile("test_content.txt", 15, 150);
        } else {
            // 正常聊天模式
            stream = chatService.generateChatResponse(message);
        }

        return ServerResponse.ok()
                .contentType(MediaType.parseMediaType("text/event-stream"))
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("X-Accel-Buffering", "no")
                .body(stream, ServerSentEvent.class);
    }

    /**
     * 流式聊天接口（POST）
     * 用于fetch API
     */
    public Mono<ServerResponse> chatStreamPost(ServerRequest request) {
        return request.body(BodyExtractors.toMono(ChatRequest.class))
                .flatMap(chatRequest -> {
                    log.info("[POST /api/chat/stream] message={}", chatRequest.getMessage());

                    Flux<ServerSentEvent<String>> stream = chatService.generateChatResponse(chatRequest.getMessage());

                    return ServerResponse.ok()
                            .contentType(MediaType.parseMediaType("text/event-stream"))
                            .header("Cache-Control", "no-cache")
                            .header("Connection", "keep-alive")
                            .header("X-Accel-Buffering", "no")
                            .body(stream, ServerSentEvent.class);
                });
    }

    /**
     * 非流式聊天接口（测试用）
     */
    public Mono<ServerResponse> chat(ServerRequest request) {
        return request.body(BodyExtractors.toMono(ChatRequest.class))
                .flatMap(chatRequest -> {
                    log.info("[POST /api/chat] message={}", chatRequest.getMessage());

                    String response = String.format("收到消息: %s",
                            chatRequest.getMessage());

                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(new ChatResponse(response,
                                    chatRequest.getSessionId() != null ? chatRequest.getSessionId() : "default"));
                });
    }

    /**
     * 回显模式流式接口（GET）
     * 用于EventSource连接
     */
    public Mono<ServerResponse> echoStreamGet(ServerRequest request) {
        String message = request.queryParam("message").orElse("");

        log.info("[GET /api/chat/echo/stream] message={}", message);

        Flux<ServerSentEvent<String>> stream = chatService.generateEchoStream(message);

        return ServerResponse.ok()
                .contentType(MediaType.parseMediaType("text/event-stream"))
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("X-Accel-Buffering", "no")
                .body(stream, ServerSentEvent.class);
    }

    /**
     * 回显模式流式接口（POST）
     * 用于fetch API
     */
    public Mono<ServerResponse> echoStreamPost(ServerRequest request) {
        return request.body(BodyExtractors.toMono(ChatRequest.class))
                .flatMap(chatRequest -> {
                    log.info("[POST /api/chat/echo/stream] message={}", chatRequest.getMessage());

                    Flux<ServerSentEvent<String>> stream = chatService.generateEchoStream(chatRequest.getMessage());

                    return ServerResponse.ok()
                            .contentType(MediaType.parseMediaType("text/event-stream"))
                            .header("Cache-Control", "no-cache")
                            .header("Connection", "keep-alive")
                            .header("X-Accel-Buffering", "no")
                            .body(stream, ServerSentEvent.class);
                });
    }

    // ==================== AI 流式接口 ====================

    /**
     * AI 流式聊天接口（GET）
     * 调用外部 AI 模型，透传流式响应
     */
    public Mono<ServerResponse> aiChatStreamGet(ServerRequest request) {
        String message = request.queryParam("message").orElse("");
        String sessionId = request.queryParam("session_id").orElse("default");

        log.info("[GET /api/ai/chat/stream] message={}, sessionId={}", message, sessionId);

        Flux<ServerSentEvent<String>> stream = aiChatService.chatStream(message, sessionId);

        return ServerResponse.ok()
                .contentType(MediaType.parseMediaType("text/event-stream"))
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("X-Accel-Buffering", "no")
                .body(stream, ServerSentEvent.class);
    }

    /**
     * AI 流式聊天接口（POST）
     * 调用外部 AI 模型，透传流式响应
     */
    public Mono<ServerResponse> aiChatStreamPost(ServerRequest request) {
        return request.body(BodyExtractors.toMono(ChatRequest.class))
                .flatMap(chatRequest -> {
                    String sessionId = chatRequest.getSessionId() != null ? chatRequest.getSessionId() : "default";
                    log.info("[POST /api/ai/chat/stream] message={}, sessionId={}", chatRequest.getMessage(), sessionId);

                    Flux<ServerSentEvent<String>> stream = aiChatService.chatStream(chatRequest.getMessage(), sessionId);

                    return ServerResponse.ok()
                            .contentType(MediaType.parseMediaType("text/event-stream"))
                            .header("Cache-Control", "no-cache")
                            .header("Connection", "keep-alive")
                            .header("X-Accel-Buffering", "no")
                            .body(stream, ServerSentEvent.class);
                });
    }

    /**
     * AI Mock 流式接口（GET）
     * 返回模拟数据，用于前端调试
     */
    public Mono<ServerResponse> aiMockStreamGet(ServerRequest request) {
        String message = request.queryParam("message").orElse("");

        log.info("[GET /api/ai/chat/mock/stream] message={}", message);

        Flux<ServerSentEvent<String>> stream = mockAiChatService.mockChatStream(message);

        return ServerResponse.ok()
                .contentType(MediaType.parseMediaType("text/event-stream"))
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("X-Accel-Buffering", "no")
                .body(stream, ServerSentEvent.class);
    }

    /**
     * AI Mock 流式接口（POST）
     * 返回模拟数据，用于前端调试
     */
    public Mono<ServerResponse> aiMockStreamPost(ServerRequest request) {
        return request.body(BodyExtractors.toMono(ChatRequest.class))
                .flatMap(chatRequest -> {
                    log.info("[POST /api/ai/chat/mock/stream] message={}", chatRequest.getMessage());

                    Flux<ServerSentEvent<String>> stream = mockAiChatService.mockChatStream(chatRequest.getMessage());

                    return ServerResponse.ok()
                            .contentType(MediaType.parseMediaType("text/event-stream"))
                            .header("Cache-Control", "no-cache")
                            .header("Connection", "keep-alive")
                            .header("X-Accel-Buffering", "no")
                            .body(stream, ServerSentEvent.class);
                });
    }

    // ==================== 响应模型 ====================

    private static class RootResponse {
        private final String message;
        private final String version;

        public RootResponse(String message, String version) {
            this.message = message;
            this.version = version;
        }

        public String getMessage() { return message; }
        public String getVersion() { return version; }
    }

    private static class HealthResponse {
        private final String status;

        public HealthResponse(String status) {
            this.status = status;
        }

        public String getStatus() { return status; }
    }

    private static class ChatResponse {
        private final String message;
        private final String sessionId;

        public ChatResponse(String message, String sessionId) {
            this.message = message;
            this.sessionId = sessionId;
        }

        public String getMessage() { return message; }
        public String getSessionId() { return sessionId; }
    }
}
