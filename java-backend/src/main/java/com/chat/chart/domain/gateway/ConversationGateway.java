package com.chat.chart.domain.gateway;

import com.chat.chart.domain.model.ChatMessage;
import com.chat.chart.domain.model.Conversation;

import java.util.List;

/**
 * 会话网关接口
 * <p>
 * 定义会话的持久化操作，包括会话的查询与创建。
 * </p>
 *
 * @author Chat Chart System
 */
public interface ConversationGateway {

    /**
     * 查询用户最近一个会话
     */
    Conversation findLatestByUserId(Long userId);

    /**
     * 查询用户所有会话列表
     */
    List<Conversation> findByUserId(Long userId);

    /**
     * 保存新会话
     */
    void saveConversation(String conversationId, Long userId);

    /**
     * 统计会话下的消息数量
     */
    int countMessagesByConversationId(String conversationId);

    /**
     * 查询会话下的所有消息
     */
    List<ChatMessage> findMessagesByConversationId(String conversationId);

    /**
     * 根据会话ID和用户ID查询会话，用于归属校验
     */
    Conversation findByConversationIdAndUserId(String conversationId, Long userId);
}
