package com.chat.chart.infrastructure.gateway;

import com.chat.chart.app.dto.InitSessionData;
import com.chat.chart.domain.gateway.AiChatGateway;
import com.chat.chart.domain.gateway.AiStreamCallback;
import com.chat.chart.infrastructure.config.AiServiceProperties;
import com.chat.chart.infrastructure.gateway.model.BaseAgentRequest;
import com.chat.chart.infrastructure.gateway.model.ChatReqData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okio.BufferedSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
    public void chatStream(String message, AiStreamCallback callback) {
        String sessionId = "";
        try {
            sessionId = initSession();

            ChatReqData chatReqData = ChatReqData.builder()
                    .sessionId(sessionId != null ? sessionId : "default")
                    .txt(message)
                    .files(new ArrayList<>())
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

            String jsonBody = objectMapper.writeValueAsString(request);

            Request httpRequest = new Request.Builder()
                    .url(properties.getUrl()+properties.getChatApi())
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody))
                    .header("Accept","text/event-stream")
                    .header("Cache-Control","no-cache")
                    .header("X-Accel-Buffering","no")
                    .build();

            Response response = httpClient.newCall(httpRequest).execute();
            log.info("[AI Chat] 已连接到外部AI服务: {}", properties.getUrl()+properties.getChatApi());

            if (!response.isSuccessful()) {
                log.error("[AI Chat] 调用外部AI服务失败, HTTP {}", response.code());
                callback.sendEvent("{\"type\":\"content\",\"data\":\"AI服务调用失败\"}");
                callback.complete();
                return;
            }

            try {
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    log.error("[AI Chat] 响应体为空");
                    callback.complete();
                    return;
                }

                try(BufferedSource source = responseBody.source()){
                    String currentEvent = null;
                    StringBuilder dataBuffer = new StringBuilder();
                    String line;

                    while((line = source.readUtf8Line()) != null){
                        log.debug("[AI Chat]收到原始行：{}",line);

                        if(line.startsWith("event:")){
                            // 如果之前有累积的事件数据，先处理完整的前一个事件
                            if (currentEvent != null && dataBuffer.length() > 0) {
                                if (processSseEvent(currentEvent, dataBuffer.toString(), callback)) {
                                    break; // 如果处理程序要求终止（如收到结束事件），则跳出循环
                                }
                            }
                            // 提取事件类型（如：message、error、end等）
                            currentEvent = line.substring(6).trim();
                            dataBuffer.setLength(0); // 清空数据缓冲区
                        } else if (line.startsWith("data:")) {
                            // 累积data字段内容，支持多行data（SSE标准允许多行data，用\n连接）
                            if (dataBuffer.length() > 0) {
                                dataBuffer.append("\n");
                            }
                            dataBuffer.append(line.substring(5).trim());
                        } else if (line.isEmpty()) {
                            // 空行表示一个SSE事件结束，处理累积的完整事件
                            if (currentEvent != null && dataBuffer.length() > 0) {
                                if (processSseEvent(currentEvent, dataBuffer.toString(), callback)) {
                                    break;
                                }
                            }
                            // 重置状态，准备接收下一个事件
                            currentEvent = null;
                            dataBuffer.setLength(0);
                        }
                    }

                    if (currentEvent != null && dataBuffer.length() > 0){
                        processSseEvent(currentEvent, dataBuffer.toString(),callback);
                    }
                }

                callback.sendEvent("{\"type\":\"end\",\"data\":null}");
            } finally {
                response.close();
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

    private String initSession() {
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
                    .post(RequestBody.create(initBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseStr = response.body().string();
                    JsonNode rootNode = objectMapper.readTree(responseStr);
                    if (rootNode.has("data") && rootNode.get("data").has("session_id")) {
                        String sessionId = rootNode.get("data").get("session_id").asText();
                        log.info("[AI Chat] 获取到session_id: {}", sessionId);
                        return sessionId;
                    }
                }
            }
        } catch (Exception e) {
            log.error("[AI Chat] 初始化失败", e);
        }
        return "";
    }

    private boolean processSseEvent(String eventType, String data, AiStreamCallback callback) {
        try {
            switch (eventType) {
                case "chunk":
                    JsonNode chunkNode = objectMapper.readTree(data);
                    String content = chunkNode.path("content").asText();
                    if (content != null && !content.isEmpty()) {
                        log.debug("[AI Chunk] 时间:{} 内容:[{}]",
                                System.currentTimeMillis(), content.replace("\n", "\\n"));
                        // 包装为统一JSON格式，前端识别为增量内容
                        Map<String, Object> chunkEvent = new HashMap<>();
                        chunkEvent.put("type", "content");
                        chunkEvent.put("data", content);
                        callback.sendEvent(objectMapper.writeValueAsString(chunkEvent));
                    }
                    break;
                case "message":
                    JsonNode msgNode = objectMapper.readTree(data);
                    String nodeId = msgNode.path("additional_kwargs").path("node_id").asText();
                    String nodeTitle = msgNode.path("additional_kwargs").path("node_title").asText();
    
                    // 关键修改：提取最终结构化输出（通常在end节点）
                    JsonNode outputNode = msgNode.path("additional_kwargs").path("node_output").path("output");
                    if (!outputNode.isMissingNode() && !outputNode.asText().isEmpty()) {
                        String finalOutput = outputNode.asText();
                        // 构建最终输出事件，前端识别后覆盖之前的内容
                        Map<String, Object> finalEvent = new HashMap<>();
                        finalEvent.put("type", "final_output"); // 前端覆盖标志
                        finalEvent.put("data", finalOutput);
                        callback.sendEvent(objectMapper.writeValueAsString(finalEvent));
                        log.info("[AI Chat] 发送最终输出: nodeId={}, length={}",
                                nodeId, finalOutput.length());
                    }
    
                    log.debug("[AI Chat] 节点执行: {} - {}", nodeId, nodeTitle);
                    break;
                case "chat_started":
                    log.info("[AI Chat] 会话开始: {}", data);
                    break;
                case "done":
                    log.info("[AI Chat] 流正常结束");
                    break;
                default:
                    log.warn("[AI Chat] 未知事件类型：{}",eventType);
                }
            } catch (Exception e){
                log.error("[AI Chat] 解析事件失败：{}",eventType,e);
            }
            return false;
        }
}
