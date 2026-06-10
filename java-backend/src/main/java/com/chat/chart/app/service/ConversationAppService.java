package com.chat.chart.app.service;

import com.chat.chart.client.api.ConversationAppServiceI;
import com.chat.chart.client.dto.ConversationDTO;
import com.chat.chart.client.dto.ConversationListResponse;
import com.chat.chart.client.dto.MessageDTO;
import com.chat.chart.domain.gateway.ConversationGateway;
import com.chat.chart.domain.model.ChatMessage;
import com.chat.chart.domain.model.Conversation;
import com.chat.chart.domain.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 会话应用服务
 */
@Service
public class ConversationAppService implements ConversationAppServiceI {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationAppService.class);

    private final ConversationGateway conversationGateway;

    public ConversationAppService(ConversationGateway conversationGateway) {
        this.conversationGateway = conversationGateway;
    }

    @Override
    public ConversationDTO createConversation(String userId) {
        String conversationId = IdGenerator.newConversationId();
        conversationGateway.saveConversation(conversationId, userId);
        LOGGER.info("[Conversation] 创建会话: userId={}, conversationId={}", userId, conversationId);

        ConversationDTO dto = new ConversationDTO();
        dto.setConversationId(conversationId);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setMessageCount(0);
        dto.setActive(true);
        return dto;
    }

    @Override
    public ConversationListResponse listConversations(String userId) {
        List<Conversation> conversations = conversationGateway.findByUserId(userId);
        List<ConversationDTO> dtos = new ArrayList<>();

        for (Conversation conv : conversations) {
            ConversationDTO dto = new ConversationDTO();
            dto.setConversationId(conv.getConversationId());
            dto.setCreatedAt(conv.getCreatedAt());
            dto.setMessageCount(conversationGateway.countMessagesByConversationId(conv.getConversationId()));
            dto.setActive(true);
            dtos.add(dto);
        }

        return new ConversationListResponse(dtos);
    }

    @Override
    public List<MessageDTO> getConversationMessages(String conversationId, String userId) {
        Conversation conv = conversationGateway.findByConversationIdAndUserId(conversationId, userId);
        if (conv == null) {
            LOGGER.warn("[Conversation] 会话不存在或不属于该用户: conversationId={}, userId={}", conversationId, userId);
            return Collections.emptyList();
        }
        List<ChatMessage> messages = conversationGateway.findMessagesByConversationId(conversationId);
        List<MessageDTO> dtos = new ArrayList<>();
        for (ChatMessage msg : messages) {
            MessageDTO dto = new MessageDTO();
            dto.setRequestId(msg.getRequestId());
            dto.setConversationId(msg.getConversationId());
            dto.setRole(msg.getRole());
            dto.setContent(msg.getContent());
            dto.setCreatedAt(msg.getCreatedAt());
            dtos.add(dto);
        }
        return dtos;
    }
}
