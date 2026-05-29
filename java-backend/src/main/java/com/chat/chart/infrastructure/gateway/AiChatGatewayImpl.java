package com.chat.chart.infrastructure.gateway;

import com.chat.chart.infrastructure.gateway.model.InitSessionData;
import com.chat.chart.domain.gateway.AiChatGateway;
import com.chat.chart.domain.gateway.AiStreamCallback;
import com.chat.chart.infrastructure.config.AiRateLimiter;
import com.chat.chart.infrastructure.config.AiServiceProperties;
import com.chat.chart.infrastructure.gateway.model.BaseAgentRequest;
import com.chat.chart.infrastructure.gateway.model.ChatReqData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(AiChatGatewayImpl.class);
    private static final String ADDITIONAL_KWARGS = "additional_kwargs";

    private final AiServiceProperties properties;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;
    private final AiRateLimiter aiRateLimiter;

    public AiChatGatewayImpl(AiServiceProperties properties, ObjectMapper objectMapper, AiRateLimiter aiRateLimiter) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.aiRateLimiter = aiRateLimiter;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(properties.getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void chatStream(String message, AiStreamCallback callback) {
        try {
            String cookieValue = UUID.randomUUID().toString().replace("-", "");
            String sessionId = initSession(cookieValue);
            String jsonBody = buildChatRequest(sessionId, message);
            Response response = executeChatRequest(jsonBody, cookieValue);

            if (!response.isSuccessful()) {
                LOGGER.error("[AI Chat] 调用外部AI服务失败, HTTP {}", response.code());
                callback.sendEvent("{\"type\":\"content\",\"data\":\"AI服务调用失败\"}");
                callback.complete();
                return;
            }

            readSseStream(response, callback);
            callback.sendEvent("{\"type\":\"end\",\"data\":null}");
            callback.complete();
        } catch (IOException e) {
            LOGGER.error("[AI Chat] 调用外部AI服务异常", e);
            callback.error(e);
        } catch (Exception e) {
            LOGGER.error("[AI Chat] 调用外部AI服务异常", e);
            callback.error(e);
        }
    }

    /**
     * 构建聊天请求JSON
     */
    private String buildChatRequest(String sessionId, String message) throws JsonProcessingException {
        ChatReqData chatReqData = ChatReqData.builder()
                .sessionId(sessionId != null ? sessionId : "default")
                .txt(message)
                .files(new ArrayList<>())
                .stream("true")
                .build();

        BaseAgentRequest<ChatReqData> request = new BaseAgentRequest<>(
                "chat", "CHAT", "1.0",
                String.valueOf(System.currentTimeMillis()),
                UUID.randomUUID().toString().replace("-", ""),
                chatReqData
        );

        return objectMapper.writeValueAsString(request);
    }

    /**
     * 发送HTTP请求连接AI服务
     */
    private Response executeChatRequest(String jsonBody, String cookieValue) throws IOException {
        Request httpRequest = new Request.Builder()
                .url(properties.getUrl() + properties.getChatApi())
                .post(RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8")))
                .header("Accept", "text/event-stream")
                .header("Cache-Control", "no-cache")
                .header("X-Accel-Buffering", "no")
                .header("Cookie", "sessionId=" + cookieValue)
                .build();

        Response response = httpClient.newCall(httpRequest).execute();
        LOGGER.info("[AI Chat] 已连接到外部AI服务: {}", properties.getUrl() + properties.getChatApi());
        return response;
    }

    /**
     * 逐行读取SSE响应流，解析event/data行并回调处理
     * <p>
     * SSE协议：event:行标识事件类型，data:行携带数据，空行表示事件结束。
     * 支持多行data（SSE标准允许多行data，用\n连接）。
     * </p>
     */
    private void readSseStream(Response response, AiStreamCallback callback) throws IOException {
        try {
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                LOGGER.error("[AI Chat] 响应体为空");
                callback.complete();
                return;
            }

            try (BufferedSource source = responseBody.source()) {
                String currentEvent = null;
                StringBuilder dataBuffer = new StringBuilder();
                String line = "";

                while ((line = source.readUtf8Line()) != null) {
                    LOGGER.debug("[AI Chat]收到原始行：{}", line);

                    if (line.startsWith("event:")) {
                        // 如果之前有累积的事件数据，先处理完整的前一个事件
                        if (processBufferedEvent(currentEvent, dataBuffer, callback)) {
                            return;
                        }
                        // 提取事件类型（如：message、error、end等）
                        currentEvent = line.substring(6).trim();
                        // 清空数据缓冲区
                        dataBuffer.setLength(0);
                    } else if (line.startsWith("data:")) {
                        // 累积data字段内容，支持多行data
                        if (dataBuffer.length() > 0) {
                            dataBuffer.append("\n");
                        }
                        dataBuffer.append(line.substring(5).trim());
                    } else if (line.isEmpty()) {
                        // 空行表示一个SSE事件结束，处理累积的完整事件
                        if (processBufferedEvent(currentEvent, dataBuffer, callback)) {
                            return;
                        }
                        // 重置状态，准备接收下一个事件
                        currentEvent = null;
                        dataBuffer.setLength(0);
                    }
                }

                // 处理流末尾可能残留的最后一个事件
                processBufferedEvent(currentEvent, dataBuffer, callback);
            }
        } finally {
            response.close();
        }
    }

    /**
     * 处理缓冲区中累积的SSE事件，返回true表示需要终止读取
     */
    private boolean processBufferedEvent(String currentEvent, StringBuilder dataBuffer, AiStreamCallback callback) {
        if (currentEvent == null || dataBuffer.length() == 0) {
            return false;
        }
        return processSseEvent(currentEvent, dataBuffer.toString(), callback);
    }

    /**
     * 初始化AI会话，获取session_id
     */
    private String initSession(String cookieValue) {
        try {
            InitSessionData initSessionData = InitSessionData.builder()
                    .configVariables(new ArrayList<>())
                    .build();
            BaseAgentRequest<InitSessionData> initRequest = new BaseAgentRequest<>(
                    "chat", "init", "1.0",
                    String.valueOf(System.currentTimeMillis()),
                    UUID.randomUUID().toString().replace("-", ""),
                    initSessionData
            );

            String initBody = objectMapper.writeValueAsString(initRequest);
            Request request = new Request.Builder()
                    .url(properties.getUrl() + properties.getInitApi())
                    .post(RequestBody.create(initBody, MediaType.get("application/json")))
                    .header("Cookie", "sessionId=" + cookieValue)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseStr = response.body().string();
                    JsonNode rootNode = objectMapper.readTree(responseStr);
                    if (rootNode.has("data") && rootNode.get("data").has("session_id")) {
                        String sessionId = rootNode.get("data").get("session_id").asText();
                        LOGGER.info("[AI Chat] 获取到session_id: {}", sessionId);
                        return sessionId;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("[AI Chat] 初始化失败", e);
        }
        return "";
    }

    /**
     * 根据事件类型分发处理
     */
    private boolean processSseEvent(String eventType, String data, AiStreamCallback callback) {
        try {
            switch (eventType) {
                case "chunk":
                    handleChunkEvent(data, callback);
                    break;
                case "message":
                    handleMessageEvent(data, callback);
                    break;
                case "chat_started":
                    LOGGER.info("[AI Chat] 会话开始: {}", data);
                    break;
                case "done":
                    if (data.contains("BDC201704_0144002 is over requestLimit")) {
                        LOGGER.warn("[AI Chat] 外部AI服务请求次数已达上限");
                        aiRateLimiter.markBlocked();
                        callback.error(new RuntimeException("外部AI服务请求次数已达上限，请稍后再试"));
                        return true;
                    }
                    LOGGER.info("[AI Chat] 流正常结束");
                    return true;
                default:
                    LOGGER.warn("[AI Chat] 未知事件类型：{}", eventType);
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("[AI Chat] 解析事件失败：{}", eventType, e);
        }
        return false;
    }

    /**
     * 处理增量内容事件，包装为统一JSON格式，前端识别为增量内容
     */
    private void handleChunkEvent(String data, AiStreamCallback callback) throws JsonProcessingException {
        JsonNode chunkNode = objectMapper.readTree(data);
        String content = chunkNode.path("content").asText();
        if (content != null && !content.isEmpty()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[AI Chunk] 时间:{} 内容:[{}]",
                        System.currentTimeMillis(), content.replace("\n", "\\n"));
            }
            Map<String, Object> chunkEvent = new HashMap<>();
            chunkEvent.put("type", "content");
            chunkEvent.put("data", content);
            callback.sendEvent(objectMapper.writeValueAsString(chunkEvent));
        }
    }

    /**
     * 处理消息事件，提取最终结构化输出（通常在end节点），前端识别后覆盖之前的内容
     */
    private void handleMessageEvent(String data, AiStreamCallback callback) throws JsonProcessingException {
        JsonNode msgNode = objectMapper.readTree(data);
        String nodeId = msgNode.path(ADDITIONAL_KWARGS).path("node_id").asText();
        String nodeTitle = msgNode.path(ADDITIONAL_KWARGS).path("node_title").asText();

        JsonNode outputNode = msgNode.path(ADDITIONAL_KWARGS).path("node_output").path("output");
        if (!outputNode.isMissingNode() && !outputNode.asText().isEmpty()) {
            String finalOutput = outputNode.asText();
            Map<String, Object> finalEvent = new HashMap<>();
            finalEvent.put("type", "final_output"); // 前端覆盖标志
            finalEvent.put("data", finalOutput);
            callback.sendEvent(objectMapper.writeValueAsString(finalEvent));
            LOGGER.info("[AI Chat] 发送最终输出: nodeId={}, length={}", nodeId, finalOutput.length());
        }

        LOGGER.debug("[AI Chat] 节点执行: {} - {}", nodeId, nodeTitle);
    }
}
