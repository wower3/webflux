package com.chat.chart.app.service;

import com.chat.chart.app.dto.ConversationDTO;
import com.chat.chart.app.dto.ConversationListResponse;
import com.chat.chart.domain.gateway.ConversationGateway;
import com.chat.chart.domain.model.ChatMessage;
import com.chat.chart.domain.model.Conversation;
import com.chat.chart.domain.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 会话应用服务
 */
@Service
public class ConversationAppService {

    private static final Logger log = LoggerFactory.getLogger(ConversationAppService.class);

    private final ConversationGateway conversationGateway;

    public ConversationAppService(ConversationGateway conversationGateway) {
        this.conversationGateway = conversationGateway;
    }

    public ConversationDTO createConversation(Long userId) {
        String conversationId = IdGenerator.newConversationId();
        conversationGateway.saveConversation(conversationId, userId);
        log.info("[Conversation] 创建会话: userId={}, conversationId={}", userId, conversationId);

        ConversationDTO dto = new ConversationDTO();
        dto.setConversationId(conversationId);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setMessageCount(0);
        dto.setActive(true);
        return dto;
    }

    public ConversationListResponse listConversations(Long userId) {
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

    public List<ChatMessage> getConversationMessages(String conversationId) {
        return conversationGateway.findMessagesByConversationId(conversationId);
    }
}
