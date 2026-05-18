package com.chat.chart.infrastructure.gateway;

import com.chat.chart.domain.gateway.MessageGateway;
import com.chat.chart.domain.model.ChatMessage;
import com.chat.chart.infrastructure.dataobject.ChatMessageDO;
import com.chat.chart.infrastructure.mapper.ChatMessageMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息网关实现
 * <p>
 * 基于MyBatis-Plus Mapper操作数据库，实现消息的保存与上下文查询。
 * </p>
 */
@Repository
public class MessageGatewayImpl implements MessageGateway {

    private final ChatMessageMapper chatMessageMapper;

    public MessageGatewayImpl(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }

    @Override
    public void saveMessage(String requestId, String conversationId, String role, String content) {
        ChatMessageDO dto = new ChatMessageDO();
        dto.setRequestId(requestId);
        dto.setConversationId(conversationId);
        dto.setRole(role);
        dto.setContent(content);
        dto.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(dto);
    }

    @Override
    public List<ChatMessage> findContextMessages(String conversationId, int maxRequests) {
        List<String> requestIds = chatMessageMapper.selectRecentRequestIds(conversationId, maxRequests);
        if (requestIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return chatMessageMapper.selectByIds(conversationId, requestIds).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    private ChatMessage toEntity(ChatMessageDO dto) {
        ChatMessage msg = new ChatMessage();
        msg.setRequestId(dto.getRequestId());
        msg.setConversationId(dto.getConversationId());
        msg.setRole(dto.getRole());
        msg.setContent(dto.getContent());
        msg.setCreatedAt(dto.getCreatedAt());
        return msg;
    }
}
