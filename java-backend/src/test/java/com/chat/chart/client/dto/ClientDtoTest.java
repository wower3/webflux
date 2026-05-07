package com.chat.chart.client.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * client.dto 层 DTO 测试
 * 验证搬迁后的 DTO 字段赋值/读取和工厂方法正确性
 */
class ClientDtoTest {

    @Test
    @DisplayName("ChatRequest 字段赋值正确")
    void chatRequest_fields() {
        ChatRequest req = new ChatRequest();
        req.setMessage("hello");
        req.setConversationId("conv-1");
        assertEquals("hello", req.getMessage());
        assertEquals("conv-1", req.getConversationId());
    }

    @Test
    @DisplayName("ConversationDTO 全字段赋值正确")
    void conversationDTO_fields() {
        LocalDateTime now = LocalDateTime.now();
        ConversationDTO dto = new ConversationDTO();
        dto.setConversationId("conv-1");
        dto.setCreatedAt(now);
        dto.setMessageCount(5);
        dto.setActive(true);
        assertEquals("conv-1", dto.getConversationId());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(5, dto.getMessageCount());
        assertTrue(dto.isActive());
    }

    @Test
    @DisplayName("ConversationListResponse 包装列表正确")
    void conversationListResponse_wrapsList() {
        ConversationDTO dto = new ConversationDTO();
        dto.setConversationId("conv-1");
        ConversationListResponse resp = new ConversationListResponse(java.util.Collections.singletonList(dto));
        assertNotNull(resp.getConversations());
        assertEquals(1, resp.getConversations().size());
        assertEquals("conv-1", resp.getConversations().get(0).getConversationId());
    }

    @Test
    @DisplayName("MessageDTO 全字段赋值正确")
    void messageDTO_fields() {
        LocalDateTime now = LocalDateTime.now();
        MessageDTO dto = new MessageDTO();
        dto.setRequestId("req-1");
        dto.setConversationId("conv-1");
        dto.setRole("user");
        dto.setContent("test content");
        dto.setCreatedAt(now);
        assertEquals("req-1", dto.getRequestId());
        assertEquals("conv-1", dto.getConversationId());
        assertEquals("user", dto.getRole());
        assertEquals("test content", dto.getContent());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    @DisplayName("StreamEvent.content 工厂方法正确")
    void streamEvent_contentFactory() {
        StreamEvent event = StreamEvent.content("hello");
        assertEquals("content", event.getType());
        assertEquals("hello", event.getData());
    }

    @Test
    @DisplayName("StreamEvent.end 工厂方法正确")
    void streamEvent_endFactory() {
        StreamEvent event = StreamEvent.end();
        assertEquals("end", event.getType());
        assertNull(event.getData());
    }

    @Test
    @DisplayName("StreamEventType 枚举值正确")
    void streamEventType_values() {
        assertEquals("content", StreamEventType.CONTENT.getValue());
        assertEquals("chart", StreamEventType.CHART.getValue());
        assertEquals("card", StreamEventType.CARD.getValue());
        assertEquals("end", StreamEventType.END.getValue());
    }
}
