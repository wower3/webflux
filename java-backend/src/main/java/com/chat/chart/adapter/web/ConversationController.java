package com.chat.chart.adapter.web;

import com.chat.chart.client.api.ConversationAppServiceI;
import com.chat.chart.client.dto.ConversationDTO;
import com.chat.chart.client.dto.ConversationListResponse;
import com.chat.chart.client.dto.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话控制器
 *
 * @see ConversationAppServiceI
 */
@RestController
@RequestMapping("/api")
public class ConversationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationController.class);

    private final ConversationAppServiceI conversationAppService;

    public ConversationController(ConversationAppServiceI conversationAppService) {
        this.conversationAppService = conversationAppService;
    }

    @PostMapping("/conversation")
    public ConversationDTO createConversation(@RequestParam("userId") Long userId) {
        LOGGER.info("[Conversation] 创建会话: userId={}", userId);
        return conversationAppService.createConversation(userId);
    }

    @GetMapping("/conversations")
    public ConversationListResponse listConversations(@RequestParam("userId") Long userId) {
        LOGGER.info("[Conversation] 列出会话: userId={}", userId);
        return conversationAppService.listConversations(userId);
    }

    @GetMapping("/conversation/{conversationId}/messages")
    public List<MessageDTO> getConversationMessages(
            @PathVariable String conversationId,
            @RequestParam("userId") Long userId) {
        LOGGER.info("[Conversation] 获取消息: conversationId={}, userId={}", conversationId, userId);
        return conversationAppService.getConversationMessages(conversationId, userId);
    }
}
