package com.chat.chart.app.service;

import com.chat.chart.client.api.ChatAppServiceI;
import com.chat.chart.domain.gateway.AiChatGateway;
import com.chat.chart.domain.gateway.AiStreamCallback;
import com.chat.chart.domain.gateway.ConversationGateway;
import com.chat.chart.domain.gateway.MessageGateway;
import com.chat.chart.domain.model.AdoptionStatus;
import com.chat.chart.domain.model.ChatMessage;
import com.chat.chart.domain.util.IdGenerator;
import com.chat.chart.infrastructure.config.AiRateLimiter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 聊天应用服务
 * <p>
 * 核心编排逻辑，负责完整的聊天消息处理流程：
 * <ol>
 *   <li>确定或创建会话</li>
 *   <li>保存用户消息到数据库</li>
 *   <li>获取历史上下文并组装提示词</li>
 *   <li>调用AI网关获取流式回复</li>
 *   <li>流结束后自动保存AI回复</li>
 * </ol>
 * </p>
 */
@Service
public class ChatAppService implements ChatAppServiceI {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatAppService.class);

    private static final long SSE_TIMEOUT_MS = 60000L;

    private static final int CONTEXT_MESSAGE_ROUNDS = 2;

    private final ConversationGateway conversationGateway;
    private final MessageGateway messageGateway;
    private final AiChatGateway aiChatGateway;
    private final Executor chatExecutor;
    private final ObjectMapper objectMapper;
    private final AiRateLimiter aiRateLimiter;

    public ChatAppService(ConversationGateway conversationGateway,
                          MessageGateway messageGateway,
                          AiChatGateway aiChatGateway,
                          @org.springframework.beans.factory.annotation.Qualifier("chatExecutor") Executor chatExecutor,
                          ObjectMapper objectMapper,
                          AiRateLimiter aiRateLimiter) {
        this.conversationGateway = conversationGateway;
        this.messageGateway = messageGateway;
        this.aiChatGateway = aiChatGateway;
        this.chatExecutor = chatExecutor;
        this.objectMapper = objectMapper;
        this.aiRateLimiter = aiRateLimiter;
    }

    /**
     * 处理聊天消息（流式）
     * <p>
     * 完整的聊天处理流程：会话管理 -> 保存用户消息 -> 组装上下文 -> 调用AI -> 保存AI回复。
     * 返回的SseEmitter在消费完成时会自动保存AI回复到数据库。
     * </p>
     *
     * @param message        用户发送的消息内容
     * @param userId         当前用户ID
     * @param conversationId 会话ID（可为null，为null时自动创建新会话）
     * @return SseEmitter，用于流式推送AI响应
     */
    @Override
    public SseEmitter handleMessage(String message, String userId, String conversationId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitter.onTimeout(() -> LOGGER.warn("[Chat] SSE超时, userId={}", userId));
        emitter.onError(e -> LOGGER.warn("[Chat] SSE连接异常: {}", e.getMessage()));

        chatExecutor.execute(() -> {
            try {
                // 1. 确定会话ID：已提供则复用，否则自动创建新会话
                String finalConversationId = determineConversationId(userId, conversationId);

                // 2. 生成本次请求的唯一requestId（user和assistant共用）
                String requestId = IdGenerator.newConversationId();

                // 3. 获取历史上下文消息（先于存库，避免当前消息混入历史）
                List<ChatMessage> contextMessages = messageGateway.findContextMessages(finalConversationId, CONTEXT_MESSAGE_ROUNDS);

                // 4. 持久化用户消息到数据库
                messageGateway.saveMessage(requestId, finalConversationId, "user", message, null, null);

                // 5. 将上下文和用户输入组装为完整提示词
                String fullMessage = assembleMessage(contextMessages, message);

                LOGGER.info("[Chat] 处理消息: userId={}, conversationId={}, requestId={}, messageLength={}",
                        userId, finalConversationId, requestId, message.length());

                // 6. 限流检查
                if (!aiRateLimiter.tryAcquire()) {
                    emitter.send(SseEmitter.event().data("{\"type\":\"content\",\"data\":\"请求过于频繁，请稍后再试\"}"));
                    emitter.send(SseEmitter.event().data("{\"type\":\"end\",\"data\":null}"));
                    emitter.complete();
                    return;
                }

                // 7. 调用AI网关获取流式回复，同时累积完整文本内容用于后续持久化
                aiChatGateway.chatStream(fullMessage, new ChatStreamCallback(
                        emitter, objectMapper, requestId, finalConversationId, messageGateway));
            } catch (Exception e) {
                LOGGER.error("[Chat] 处理消息失败", e);
                safeCompleteWithError(emitter, e);
            }
        });

        return emitter;
    }

    /**
     * 确定会话ID
     * <p>
     * 如果提供了有效的conversationId则直接使用，否则创建新会话并返回新ID。
     * </p>
     */
    private String determineConversationId(String userId, String conversationId) {
        if (conversationId != null && !conversationId.trim().isEmpty()) {
            return conversationId;
        }

        String newConversationId = IdGenerator.newConversationId();
        conversationGateway.saveConversation(newConversationId, userId);
        LOGGER.info("[Chat] 创建新会话: conversationId={}", newConversationId);
        return newConversationId;
    }

    /**
     * 更新一轮对话的采纳状态
     * <p>
     * 根据requestId更新该轮对话（user+assistant）中所有消息的采纳状态。
     * </p>
     *
     * @param requestId      请求ID（一轮对话的标识）
     * @param adoptionStatus 采纳状态（0-未采纳，1-已采纳，2-默认）
     */
    @Override
    public void updateAdoptionStatus(String requestId, String adoptionStatus) {
        LOGGER.info("[Chat] 更新采纳状态: requestId={}, adoptionStatus={}", requestId, adoptionStatus);
        messageGateway.updateAdoptionStatus(requestId, adoptionStatus);
    }

    /**
     * 组装带历史上下文的完整消息
     * <p>
     * 将历史对话拼接为"历史对话"格式，再附加用户当前输入，
     * 构成发送给AI的完整提示词。
     * </p>
     */
    private String assembleMessage(List<ChatMessage> contextMessages, String userInput) {
        if (contextMessages == null || contextMessages.isEmpty()) {
            return userInput;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("历史对话：\n");

        String lastRequestId = null;
        for (ChatMessage msg : contextMessages) {
            if (lastRequestId != null && !lastRequestId.equals(msg.getRequestId())) {
                sb.append("\n");
            }
            String roleName = "user".equals(msg.getRole()) ? "用户" : "助手";
            sb.append(roleName).append("：").append(msg.getContent()).append("\n");
            lastRequestId = msg.getRequestId();
        }

        sb.append("\n---\n").append("当前问题：").append(userInput);
        return sb.toString();
    }

    private static void safeCompleteWithError(SseEmitter emitter, Throwable e) {
        try {
            emitter.completeWithError(e);
        } catch (Exception ex) {
            LOGGER.debug("[Chat] emitter已关闭：{}", ex.getMessage());
        }
    }

    /**
     * AI流式回调实现，负责SSE事件转发、最终输出捕获和AI回复持久化
     */
    private static class ChatStreamCallback implements AiStreamCallback {

        private final SseEmitter emitter;
        private final ObjectMapper objectMapper;
        private final String requestId;
        private final String conversationId;
        private final MessageGateway messageGateway;
        private final AtomicReference<String> finalOutputRef = new AtomicReference<>();

        ChatStreamCallback(SseEmitter emitter, ObjectMapper objectMapper,
                           String requestId, String conversationId, MessageGateway messageGateway) {
            this.emitter = emitter;
            this.objectMapper = objectMapper;
            this.requestId = requestId;
            this.conversationId = conversationId;
            this.messageGateway = messageGateway;
        }

        @Override
        public void sendEvent(String data) {
            try {
                JsonNode node = objectMapper.readTree(data);
                if ("end".equals(node.path("type").asText())) {
                    com.fasterxml.jackson.databind.node.ObjectNode enriched =
                            (com.fasterxml.jackson.databind.node.ObjectNode) node;
                    com.fasterxml.jackson.databind.node.ObjectNode dataNode =
                            enriched.has("data") && enriched.get("data").isObject()
                                    ? (com.fasterxml.jackson.databind.node.ObjectNode) enriched.get("data")
                                    : enriched.putObject("data");
                    dataNode.put("requestId", requestId);
                    emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(enriched)));
                } else {
                    emitter.send(SseEmitter.event().data(data));
                }
                if ("final_output".equals(node.path("type").asText())) {
                    finalOutputRef.set(node.path("data").asText());
                    LOGGER.debug("[Chat] 捕获最终输出：requestId={}, length={}",
                            requestId, finalOutputRef.get().length());
                }
            } catch (Exception e) {
                LOGGER.debug("[Chat] SSE发送失败(客户端可能已断开)：{}", e.getMessage());
            }
        }

        @Override
        public void complete() {
            String content = finalOutputRef.get();
            if (content != null && !content.trim().isEmpty()) {
                saveAssistantReply(content, "1");
            }
            try {
                emitter.complete();
            } catch (Exception e) {
                LOGGER.debug("[Chat] emitter已关闭：{}", e.getMessage());
            }
        }

        @Override
        public void error(Throwable e) {
            String content = finalOutputRef.get();
            if (content != null && !content.trim().isEmpty()) {
                saveAssistantReply(content, "0");
            } else {
                saveAssistantReply("", "0");
            }
            safeCompleteWithError(emitter, e);
        }

        private void saveAssistantReply(String content, String isSuccess) {
            try {
                messageGateway.saveMessage(requestId, conversationId, "assistant", content, isSuccess, AdoptionStatus.DEFAULT.getValue());
                LOGGER.info("[Chat] 保存AI回复：conversationId={}, requestId={}, isSuccess={}",
                        conversationId, requestId, isSuccess);
            } catch (Exception ex) {
                LOGGER.error("[Chat] 保存AI回复失败", ex);
            }
        }
    }
}
