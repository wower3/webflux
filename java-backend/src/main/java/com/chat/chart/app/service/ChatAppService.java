package com.chat.chart.app.service;

import com.chat.chart.domain.gateway.ConversationGateway;
import com.chat.chart.domain.gateway.MessageGateway;
import com.chat.chart.domain.gateway.MockAiChatGateway;
import com.chat.chart.domain.model.ChatMessage;
import com.chat.chart.domain.model.Conversation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import com.chat.chart.infrastructure.util.IdGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    /**
     * 会话数据网关
     */
    private final ConversationGateway conversationGateway;

    /**
     * 消息数据网关
     */
    private final MessageGateway messageGateway;

    /**
     * 模拟AI聊天网关（用于开发测试）
     */
    private final MockAiChatGateway mockAiChatGateway;

    /**
     * 构造函数注入所有依赖
     *
     * @param conversationGateway 会话数据网关
     * @param messageGateway      消息数据网关
     * @param mockAiChatGateway   模拟AI聊天网关
     */
    public ChatAppService(ConversationGateway conversationGateway,
                          MessageGateway messageGateway,
                          MockAiChatGateway mockAiChatGateway) {
        this.conversationGateway = conversationGateway;
        this.messageGateway = messageGateway;
        this.mockAiChatGateway = mockAiChatGateway;
    }

    /**
     * 处理聊天消息
     * <p>
     * 完整的聊天处理流程：会话管理 -> 保存用户消息 -> 组装上下文 -> 调用AI -> 保存AI回复。
     * 返回的SSE流在消费完成时会自动触发close函数保存AI回复。
     * </p>
     *
     * @param message        用户发送的消息内容
     * @param userId         当前用户ID
     * @param conversationId 会话ID（可为null，为null时自动创建新会话）
     * @return 包含SSE流、关闭函数和会话ID的结果对象
     */
    public Mono<ChatStreamResult> handleMessage(String message, Long userId, String conversationId) {
        return Mono.fromCallable(() -> {
            // 1. 确定会话ID：已提供则复用，否则自动创建新会话
            String finalConversationId = determineConversationId(userId, conversationId);

            // 2. 生成本次请求的唯一requestId（user和assistant共用）
            String requestId = IdGenerator.newConversationId();

            // 3. 持久化用户消息到数据库
            messageGateway.saveMessage(requestId, finalConversationId, "user", message);

            // 4. 获取历史上下文消息（最近2个request的对话）
            List<ChatMessage> contextMessages = messageGateway.findContextMessages(finalConversationId, 2);

            // 5. 将上下文和用户输入组装为完整提示词
            String fullMessage = assembleMessage(contextMessages, message);

            log.info("[Chat] 处理消息: userId={}, conversationId={}, requestId={}, messageLength={}",
                    userId, finalConversationId, requestId, message.length());

            return new ChatStreamContext(finalConversationId, requestId, fullMessage);
        }).subscribeOn(Schedulers.boundedElastic()).flatMap(ctx -> {

            // 6. 调用AI网关获取流式回复，同时累积完整文本内容用于后续持久化
            StringBuilder contentAccumulator = new StringBuilder();

            Flux<ServerSentEvent<String>> stream = mockAiChatGateway.mockChatStream(ctx.getFullMessage())
                    .doOnNext(sse -> {
                        if (sse.data() != null) {
                            try {
                                // 尝试解析JSON格式的事件数据，提取content类型的内容
                                JsonNode node = new ObjectMapper().readTree(sse.data());
                                if ("content".equals(node.path("type").asText())) {
                                    contentAccumulator.append(node.path("data").asText(""));
                                }
                            } catch (Exception e) {
                                // 非JSON数据，直接作为文本追加
                                contentAccumulator.append(sse.data());
                            }
                        }
                    });

            // 7. 创建close函数：SSE流结束后保存assistant消息到数据库
            Runnable closeFunction = () -> {
                String assistantContent = contentAccumulator.toString();
                if (assistantContent != null && !assistantContent.trim().isEmpty()) {
                    try {
                        messageGateway.saveMessage(
                                ctx.getRequestId(),
                                ctx.getConversationId(),
                                "assistant",
                                assistantContent
                        );
                        log.info("[Chat] 保存AI回复: conversationId={}, requestId={}, contentLength={}",
                                ctx.getConversationId(), ctx.getRequestId(), assistantContent.length());
                    } catch (Exception e) {
                        log.error("[Chat] 保存AI回复失败", e);
                    }
                }
            };

            return Mono.just(new ChatStreamResult(stream, closeFunction, ctx.getConversationId()));
        });
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

    /**
     * 聊天流上下文（内部使用）
     * <p>
     * 在消息处理流程中暂存会话ID、requestId和完整提示词，
     * 用于在AI流式回调中持久化消息。
     * </p>
     */
    private static class ChatStreamContext {
        /** 会话ID */
        private final String conversationId;
        /** 本次请求的requestId */
        private final String requestId;
        /** 组装后的完整提示词 */
        private final String fullMessage;

        ChatStreamContext(String conversationId, String requestId, String fullMessage) {
            this.conversationId = conversationId;
            this.requestId = requestId;
            this.fullMessage = fullMessage;
        }

        String getConversationId() { return conversationId; }
        String getRequestId() { return requestId; }
        String getFullMessage() { return fullMessage; }
    }

    /**
     * 聊天流结果
     * <p>
     * 封装SSE流、关闭函数和会话ID。
     * 当调用 {@link #getStream()} 时，流完成时会自动触发close函数保存AI回复。
     * </p>
     */
    public static class ChatStreamResult {
        /** SSE事件流 */
        private final Flux<ServerSentEvent<String>> stream;
        /** 流结束后的回调函数，用于保存AI回复 */
        private final Runnable closeFunction;
        /** 本次消息所属的会话ID */
        private final String conversationId;

        public ChatStreamResult(Flux<ServerSentEvent<String>> stream, Runnable closeFunction, String conversationId) {
            this.stream = stream;
            this.closeFunction = closeFunction;
            this.conversationId = conversationId;
        }

        /**
         * 获取SSE流，流完成时自动执行close函数
         *
         * @return 绑定了完成回调的SSE流
         */
        public Flux<ServerSentEvent<String>> getStream() {
            return stream.doOnComplete(closeFunction);
        }

        public Runnable getCloseFunction() {
            return closeFunction;
        }

        public String getConversationId() {
            return conversationId;
        }
    }
}
