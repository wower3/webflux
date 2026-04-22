package com.chat.chart.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 会话列表响应DTO
 * <p>
 * 封装用户的所有会话列表，用于前端侧边栏展示。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationListResponse {

    /**
     * 会话DTO列表
     */
    private List<ConversationDTO> conversations;
}
