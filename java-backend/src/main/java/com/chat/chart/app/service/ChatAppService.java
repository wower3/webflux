package com.chat.chart.app.service;

import com.chat.chart.domain.gateway.AiStreamCallback;
import com.chat.chart.domain.gateway.ConversationGateway;
import com.chat.chart.domain.gateway.MessageGateway;
import com.chat.chart.domain.gateway.MockAiChatGateway;
import com.chat.chart.domain.model.ChatMessage;
import com.chat.chart.domain.util.IdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.Executor;

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
public class ChatAppService {

    private static final Logger log = LoggerFactory.getLogger(ChatAppService.class);

    private static final long SSE_TIMEOUT_MS = 60000L;

    private static final int CONTEXT_MESSAGE_ROUNDS = 2;

    /** 会话数据网关 */
    private final ConversationGateway conversationGateway;

    /** 消息数据网关 */
    private final MessageGateway messageGateway;

    /** 模拟AI聊天网关（用于开发测试） */
    private final MockAiChatGateway mockAiChatGateway;

    /** 聊天专用线程池 */
    private final Executor chatExecutor;

    private final ObjectMapper objectMapper;

    public ChatAppService(ConversationGateway conversationGateway,
                          MessageGateway messageGateway,
                          MockAiChatGateway mockAiChatGateway,
                          Executor chatExecutor,
                          ObjectMapper objectMapper) {
        this.conversationGateway = conversationGateway;
        this.messageGateway = messageGateway;
        this.mockAiChatGateway = mockAiChatGateway;
        this.chatExecutor = chatExecutor;
        this.objectMapper = objectMapper;
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
    public SseEmitter handleMessage(String message, Long userId, String conversationId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitter.onTimeout(() -> log.warn("[Chat] SSE超时, userId={}", userId));
        emitter.onError(e -> log.warn("[Chat] SSE连接异常: {}", e.getMessage()));

        chatExecutor.execute(() -> {
            StringBuilder contentAccumulator = new StringBuilder();
            try {
                // 1. 确定会话ID：已提供则复用，否则自动创建新会话
                String finalConversationId = determineConversationId(userId, conversationId);

                // 2. 生成本次请求的唯一requestId（user和assistant共用）
                String requestId = IdGenerator.newConversationId();

                // 3. 持久化用户消息到数据库
                messageGateway.saveMessage(requestId, finalConversationId, "user", message);

                // 4. 获取历史上下文消息（最近2个request的对话）
                List<ChatMessage> contextMessages = messageGateway.findContextMessages(finalConversationId, CONTEXT_MESSAGE_ROUNDS);

                // 5. 将上下文和用户输入组装为完整提示词
                String fullMessage = assembleMessage(contextMessages, message);

                log.info("[Chat] 处理消息: userId={}, conversationId={}, requestId={}, messageLength={}",
                        userId, finalConversationId, requestId, message.length());

                // 6. 调用AI网关获取流式回复，同时累积完整文本内容用于后续持久化
                mockAiChatGateway.mockChatStream(fullMessage, new AiStreamCallback() {
                    @Override
                    public void sendEvent(String data) {
                        try {
                            emitter.send(SseEmitter.event().data(data));
                            // 尝试解析JSON格式的事件数据，提取content类型的内容
                            try {
                                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(data);
                                if ("content".equals(node.path("type").asText())) {
                                    contentAccumulator.append(node.path("data").asText(""));
                                }
                            } catch (Exception e) {
                                // 非JSON数据，直接作为文本追加
                                contentAccumulator.append(data);
                            }
                        } catch (Exception e) {
                            log.error("[Chat] 发送SSE事件失败", e);
                        }
                    }

                    @Override
                    public void complete() {
                        // 不在此处complete emitter，因为还需要保存AI回复
                    }

                    @Override
                    public void error(Throwable e) {
                        emitter.completeWithError(e);
                    }
                });

                // 7. 保存AI回复到数据库
                String assistantContent = contentAccumulator.toString();
                if (assistantContent != null && !assistantContent.trim().isEmpty()) {
                    try {
                        messageGateway.saveMessage(
                                requestId,
                                finalConversationId,
                                "assistant",
                                assistantContent
                        );
                        log.info("[Chat] 保存AI回复: conversationId={}, requestId={}, contentLength={}",
                                finalConversationId, requestId, assistantContent.length());
                    } catch (Exception e) {
                        log.error("[Chat] 保存AI回复失败", e);
                    }
                }

                // 8. 流式响应完成，关闭emitter
                emitter.complete();
            } catch (Exception e) {
                log.error("[Chat] 处理消息失败", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 处理聊天消息（非流式）
     * <p>
     * 接收用户消息，等待AI完整响应后一次性返回文本内容。
     * 适用于不需要实时流式展示的场景。
     * </p>
     * <p>
     * TODO: 当前未使用，待AI真实接入后根据需要启用或移除。
     * </p>
     *
     * @param message        用户发送的消息内容
     * @param userId         当前用户ID
     * @param conversationId 会话ID（可为null，为null时自动创建新会话）
     * @return AI的完整回复文本
     */
    // @Deprecated // 非流式处理，当前未使用，待确认后启用或移除
    public String handleMessageSync(String message, Long userId, String conversationId) {
        String finalConversationId = determineConversationId(userId, conversationId);
        String requestId = IdGenerator.newConversationId();

        // 保存用户消息
        messageGateway.saveMessage(requestId, finalConversationId, "user", message);

        // 获取上下文并组装提示词
        List<ChatMessage> contextMessages = messageGateway.findContextMessages(finalConversationId, CONTEXT_MESSAGE_ROUNDS);
        String fullMessage = assembleMessage(contextMessages, message);

        log.info("[Chat] 处理消息(同步): userId={}, conversationId={}, requestId={}",
                userId, finalConversationId, requestId);

        // TODO: 调用真实AI服务，当前返回固定文本
        String assistantContent = "好的，已收到您的消息。";
        messageGateway.saveMessage(requestId, finalConversationId, "assistant", assistantContent);

        return assistantContent;
    }

    /**
     * 确定会话ID
     * <p>
     * 如果提供了有效的conversationId则直接使用，否则创建新会话并返回新ID。
     * </p>
     *
     * @param userId         用户ID
     * @param conversationId 前端传入的会话ID（可为null或空）
     * @return 确定后的会话ID
     */
    private String determineConversationId(Long userId, String conversationId) {
        if (conversationId != null && !conversationId.trim().isEmpty()) {
            return conversationId;
        }

        // 创建新会话
        String newConversationId = IdGenerator.newConversationId();
        conversationGateway.saveConversation(newConversationId, userId);
        log.info("[Chat] 创建新会话: conversationId={}", newConversationId);
        return newConversationId;
    }

    /**
     * 组装带历史上下文的完整消息
     * <p>
     * 将历史对话拼接为"历史对话"格式，再附加用户当前输入，
     * 构成发送给AI的完整提示词。
     * </p>
     *
     * @param contextMessages 历史上下文消息列表
     * @param userInput       用户当前输入
     * @return 组装后的完整提示词字符串
     */
    private String assembleMessage(List<ChatMessage> contextMessages, String userInput) {
        if (contextMessages == null || contextMessages.isEmpty()) {
            return userInput;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("历史对话：\n");

        // 将每条历史消息按角色格式化
        for (ChatMessage msg : contextMessages) {
            String roleName = "user".equals(msg.getRole()) ? "用户" : "助手";
            sb.append(roleName).append("：").append(msg.getContent()).append("\n");
        }

        sb.append("用户输入：").append(userInput);
        return sb.toString();
    }
}
