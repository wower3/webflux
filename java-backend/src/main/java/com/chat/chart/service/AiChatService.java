package com.chat.chart.service;

import com.chat.chart.model.AiServiceProperties;
import com.chat.chart.model.BaseAgentRequest;
import com.chat.chart.model.ChatReqData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

/**
 * AI 模型流式调用服务
 * 调用外部 AI 模型接口，透传流式响应
 */
@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);
    private final AiServiceProperties properties;

    public AiChatService(AiServiceProperties properties) {
        this.properties = properties;
    }

    /**
     * 调用外部 AI 模型流式接口
     *
     * @param message   用户消息
     * @param sessionId 会话ID
     * @return SSE 事件流
     */
    public Flux<ServerSentEvent<String>> chatStream(String message, String sessionId) {
        String fullUrl = properties.getUrl() + properties.getChatApi();
        log.info("[AI Chat] 调用外部AI服务, url={}, message={}, sessionId={}", fullUrl, message, sessionId);

        ChatReqData chatReqData = ChatReqData.builder()
                .sessionId(sessionId != null ? sessionId : "default")
                .txt(message)
                .stream("true")
                .build();

        BaseAgentRequest<ChatReqData> request = new BaseAgentRequest<>(
                "chat",
                "CHAT",
                "1.0",
                String.valueOf(System.currentTimeMillis()),
                UUID.randomUUID().toString().replace("-", ""),
                chatReqData
        );

        WebClient webClient = WebClient.create();

        return webClient.post()
                .uri(fullUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .timeout(Duration.ofMillis(properties.getTimeout()))
                .doOnSubscribe(s -> log.info("[AI Chat] 已连接到外部AI服务: {}", fullUrl))
                .doOnError(e -> log.error("[AI Chat] 调用外部AI服务失败: {}", fullUrl, e));
    }
}
