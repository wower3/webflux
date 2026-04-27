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

    Conversation findLatestByUserId(Long userId);

    List<Conversation> findByUserId(Long userId);

    void saveConversation(String conversationId, Long userId);

    int countMessagesByConversationId(String conversationId);

    List<ChatMessage> findMessagesByConversationId(String conversationId);
}
