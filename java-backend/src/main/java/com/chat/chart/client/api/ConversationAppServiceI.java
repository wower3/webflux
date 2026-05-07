package com.chat.chart.client.api;

import com.chat.chart.client.dto.ConversationDTO;
import com.chat.chart.client.dto.ConversationListResponse;
import com.chat.chart.client.dto.MessageDTO;

import java.util.List;

/**
 * 会话应用服务接口
 */
public interface ConversationAppServiceI {

    /**
     * 创建新会话
     *
     * @param userId 用户ID
     * @return 会话信息
     */
    ConversationDTO createConversation(Long userId);

    /**
     * 获取用户的所有会话列表
     *
     * @param userId 用户ID
     * @return 会话列表响应
     */
    ConversationListResponse listConversations(Long userId);

    /**
     * 获取指定会话的历史消息
     *
     * @param conversationId 会话ID
     * @return 消息DTO列表
     */
    List<MessageDTO> getConversationMessages(String conversationId, Long userId);
}
