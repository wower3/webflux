package com.chat.chart.app.service;

import com.chat.chart.client.dto.ConversationDTO;
import com.chat.chart.client.dto.ConversationListResponse;
import com.chat.chart.client.dto.MessageDTO;
import com.chat.chart.domain.gateway.ConversationGateway;
import com.chat.chart.domain.model.ChatMessage;
import com.chat.chart.domain.model.Conversation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConversationAppServiceTest {

    private ConversationGateway conversationGateway;
    private ConversationAppService conversationAppService;

    @BeforeEach
    void setUp() {
        conversationGateway = mock(ConversationGateway.class);
        conversationAppService = new ConversationAppService(conversationGateway);
    }

    @Test
    @DisplayName("创建会话返回正确的 ConversationDTO")
    void createConversation_returnsDto() {
        ConversationDTO dto = conversationAppService.createConversation(1L);

        assertNotNull(dto.getConversationId());
        assertNotNull(dto.getCreatedAt());
        assertEquals(0, dto.getMessageCount());
        assertTrue(dto.isActive());
        verify(conversationGateway).saveConversation(anyString(), eq(1L));
    }

    @Test
    @DisplayName("列出会话返回 ConversationListResponse")
    void listConversations_returnsResponse() {
        Conversation conv = new Conversation();
        conv.setConversationId("conv-1");
        conv.setCreatedAt(LocalDateTime.now());
        when(conversationGateway.findByUserId(1L)).thenReturn(Collections.singletonList(conv));
        when(conversationGateway.countMessagesByConversationId("conv-1")).thenReturn(3);

        ConversationListResponse resp = conversationAppService.listConversations(1L);

        assertNotNull(resp.getConversations());
        assertEquals(1, resp.getConversations().size());
        assertEquals("conv-1", resp.getConversations().get(0).getConversationId());
        assertEquals(3, resp.getConversations().get(0).getMessageCount());
    }

    @Test
    @DisplayName("getConversationMessages 会话属于该用户时返回消息")
    void getConversationMessages_ownerReturnsMessages() {
        Conversation conv = new Conversation();
        conv.setConversationId("conv-1");
        conv.setUserId(1L);
        when(conversationGateway.findByConversationIdAndUserId("conv-1", 1L)).thenReturn(conv);

        ChatMessage msg = new ChatMessage();
        msg.setRequestId("req-1");
        msg.setConversationId("conv-1");
        msg.setRole("user");
        msg.setContent("hello");
        msg.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        when(conversationGateway.findMessagesByConversationId("conv-1"))
                .thenReturn(Collections.singletonList(msg));

        List<MessageDTO> dtos = conversationAppService.getConversationMessages("conv-1", 1L);

        assertEquals(1, dtos.size());
        assertEquals("hello", dtos.get(0).getContent());
        assertEquals("user", dtos.get(0).getRole());
    }

    @Test
    @DisplayName("getConversationMessages 会话不属于该用户时返回空列表")
    void getConversationMessages_notOwnerReturnsEmpty() {
        when(conversationGateway.findByConversationIdAndUserId("conv-1", 2L)).thenReturn(null);

        List<MessageDTO> dtos = conversationAppService.getConversationMessages("conv-1", 2L);

        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
        verify(conversationGateway, never()).findMessagesByConversationId(anyString());
    }

    @Test
    @DisplayName("getConversationMessages 多条消息全部转换正确")
    void getConversationMessages_multipleMessages() {
        Conversation conv = new Conversation();
        conv.setConversationId("conv-1");
        conv.setUserId(1L);
        when(conversationGateway.findByConversationIdAndUserId("conv-1", 1L)).thenReturn(conv);

        ChatMessage msg1 = new ChatMessage();
        msg1.setRequestId("req-1");
        msg1.setConversationId("conv-1");
        msg1.setRole("user");
        msg1.setContent("hi");
        msg1.setCreatedAt(LocalDateTime.now());

        ChatMessage msg2 = new ChatMessage();
        msg2.setRequestId("req-1");
        msg2.setConversationId("conv-1");
        msg2.setRole("assistant");
        msg2.setContent("hello");
        msg2.setCreatedAt(LocalDateTime.now());

        when(conversationGateway.findMessagesByConversationId("conv-1"))
                .thenReturn(Arrays.asList(msg1, msg2));

        List<MessageDTO> dtos = conversationAppService.getConversationMessages("conv-1", 1L);

        assertEquals(2, dtos.size());
        assertEquals("user", dtos.get(0).getRole());
        assertEquals("assistant", dtos.get(1).getRole());
    }
}
