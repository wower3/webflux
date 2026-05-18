package com.chat.chart.infrastructure.gateway;

import com.chat.chart.domain.gateway.ConversationGateway;
import com.chat.chart.domain.model.ChatMessage;
import com.chat.chart.domain.model.Conversation;
import com.chat.chart.infrastructure.dataobject.ChatMessageDO;
import com.chat.chart.infrastructure.dataobject.ConversationDO;
import com.chat.chart.infrastructure.mapper.ChatMessageMapper;
import com.chat.chart.infrastructure.mapper.ConversationMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话网关实现
 * <p>
 * 基于MyBatis-Plus Mapper操作数据库，实现会话的查询与持久化。
 * </p>
 */
@Repository
public class ConversationGatewayImpl implements ConversationGateway {

    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;

    public ConversationGatewayImpl(ConversationMapper conversationMapper, ChatMessageMapper chatMessageMapper) {
        this.conversationMapper = conversationMapper;
        this.chatMessageMapper = chatMessageMapper;
    }

    @Override
    public Conversation findLatestByUserId(Long userId) {
        ConversationDO dto = conversationMapper.selectLatestByUserId(userId);
        return dto != null ? toEntity(dto) : null;
    }

    @Override
    public List<Conversation> findByUserId(Long userId) {
        return conversationMapper.selectByUserId(userId).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void saveConversation(String conversationId, Long userId) {
        ConversationDO dto = new ConversationDO();
        dto.setConversationId(conversationId);
        dto.setUserId(userId);
        dto.setCreatedAt(LocalDateTime.now());
        conversationMapper.insert(dto);
    }

    @Override
    public int countMessagesByConversationId(String conversationId) {
        return conversationMapper.countMessagesByConversationId(conversationId);
    }

    @Override
    public List<ChatMessage> findMessagesByConversationId(String conversationId) {
        return chatMessageMapper.selectByConversationId(conversationId).stream()
                .map(this::toMessageEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Conversation findByConversationIdAndUserId(String conversationId, Long userId) {
        ConversationDO dto = conversationMapper.selectByConversationIdAndUserId(conversationId, userId);
        return dto != null ? toEntity(dto) : null;
    }

    private Conversation toEntity(ConversationDO dto) {
        Conversation conv = new Conversation();
        conv.setConversationId(dto.getConversationId());
        conv.setUserId(dto.getUserId());
        conv.setCreatedAt(dto.getCreatedAt());
        return conv;
    }

    private ChatMessage toMessageEntity(ChatMessageDO dto) {
        ChatMessage msg = new ChatMessage();
        msg.setRequestId(dto.getRequestId());
        msg.setConversationId(dto.getConversationId());
        msg.setRole(dto.getRole());
        msg.setContent(dto.getContent());
        msg.setCreatedAt(dto.getCreatedAt());
        return msg;
    }
}
