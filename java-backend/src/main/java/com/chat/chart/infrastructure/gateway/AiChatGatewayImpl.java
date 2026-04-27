package com.chat.chart.infrastructure.gateway;

import com.chat.chart.domain.gateway.AiChatGateway;
import com.chat.chart.domain.gateway.AiStreamCallback;
import com.chat.chart.infrastructure.config.AiServiceProperties;
import com.chat.chart.infrastructure.gateway.model.BaseAgentRequest;
import com.chat.chart.infrastructure.gateway.model.ChatReqData;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * AI模型流式调用网关实现
 * <p>
 * 通过OkHttp调用外部AI模型接口，逐行读取SSE响应流，
 * 并通过回调接口将事件数据传递给调用方。
 * </p>
 *
 * @author Chat Chart System
 */
@Repository
public class AiChatGatewayImpl implements AiChatGateway {

    private static final Logger log = LoggerFactory.getLogger(AiChatGatewayImpl.class);

    private final AiServiceProperties properties;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public AiChatGatewayImpl(AiServiceProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(properties.getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void chatStream(String message, String sessionId, AiStreamCallback callback) {
        try {
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

            String body = objectMapper.writeValueAsString(request);

            Request httpRequest = new Request.Builder()
                    .url(fullUrl)
                    .post(RequestBody.create(body, MediaType.parse("application/json; charset=utf-8")))
                    .build();

            Response response = httpClient.newCall(httpRequest).execute();
            log.info("[AI Chat] 已连接到外部AI服务: {}", fullUrl);

            if (!response.isSuccessful()) {
                log.error("[AI Chat] 调用外部AI服务失败, HTTP {}", response.code());
                callback.sendEvent("{\"type\":\"content\",\"data\":\"AI服务调用失败\"}");
                callback.complete();
                return;
            }

            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    log.error("[AI Chat] 响应体为空");
                    callback.complete();
                    return;
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        if (!data.isEmpty()) {
                            callback.sendEvent(data);
                        }
                    }
                }
            }

            callback.complete();
        } catch (IOException e) {
            log.error("[AI Chat] 调用外部AI服务异常", e);
            callback.error(e);
        } catch (Exception e) {
            log.error("[AI Chat] 调用外部AI服务异常", e);
            callback.error(e);
        }
    }
}
