package com.chat.chart.domain.gateway;

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
     * 查找用户最新的会话
     *
     * @param userId 用户ID
     * @return 最新会话，不存在返回null
     */
    Conversation findLatestByUserId(Long userId);

    /**
     * 查找用户所有会话
     * <p>
     * 按创建时间倒序返回用户的全部会话列表。
     * </p>
     *
     * @param userId 用户ID
     * @return 会话列表，按创建时间倒序排列
     */
    List<Conversation> findByUserId(Long userId);

    /**
     * 保存新会话
     *
     * @param conversationId 会话唯一标识
     * @param userId         所属用户ID
     */
    void saveConversation(String conversationId, Long userId);
}
