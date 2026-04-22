package com.chat.chart.infrastructure.gateway;

import com.chat.chart.domain.gateway.AiChatGateway;
import com.chat.chart.infrastructure.config.AiServiceProperties;
import com.chat.chart.infrastructure.gateway.model.BaseAgentRequest;
import com.chat.chart.infrastructure.gateway.model.ChatReqData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

/**
 * AI模型流式调用网关实现
 * <p>
 * 通过WebClient调用外部AI模型接口，将请求封装为标准的Agent协议格式，
 * 并透传返回的SSE流式响应。
 * </p>
 *
 * @author Chat Chart System
 */
@Repository
public class AiChatGatewayImpl implements AiChatGateway {

    private static final Logger log = LoggerFactory.getLogger(AiChatGatewayImpl.class);

    /** AI服务配置属性 */
    private final AiServiceProperties properties;

    /**
     * 构造函数
     *
     * @param properties AI服务配置属性
     */
    public AiChatGatewayImpl(AiServiceProperties properties) {
        this.properties = properties;
    }

    /**
     * AI流式聊天
     * <p>
     * 构建Agent协议请求体，通过WebClient向外部AI服务发起POST请求，
     * 接收并透传SSE格式的流式响应。
     * </p>
     *
     * @param message   用户消息内容
     * @param sessionId 会话ID
     * @return SSE事件流
     */
    @Override
    public Flux<ServerSentEvent<String>> chatStream(String message, String sessionId) {
        // 拼接完整的API地址
        String fullUrl = properties.getUrl() + properties.getChatApi();
        log.info("[AI Chat] 调用外部AI服务, url={}, message={}, sessionId={}", fullUrl, message, sessionId);

        // 构建聊天请求数据
        ChatReqData chatReqData = ChatReqData.builder()
                .sessionId(sessionId != null ? sessionId : "default")
                .txt(message)
                .stream("true")
                .build();

        // 封装为Agent协议请求格式
        BaseAgentRequest<ChatReqData> request = new BaseAgentRequest<>(
                "chat",                                         // 应用ID
                "CHAT",                                         // 交易码
                "1.0",                                          // 协议版本
                String.valueOf(System.currentTimeMillis()),    // 时间戳
                UUID.randomUUID().toString().replace("-", ""),   // 请求唯一标识
                chatReqData                                     // 业务数据
        );

        WebClient webClient = WebClient.create();

        return webClient.post()
                .uri(fullUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .timeout(Duration.ofMillis(properties.getTimeout()))         // 设置超时时间
                .doOnSubscribe(s -> log.info("[AI Chat] 已连接到外部AI服务: {}", fullUrl))
                .doOnError(e -> log.error("[AI Chat] 调用外部AI服务失败: {}", fullUrl, e));
    }
}
