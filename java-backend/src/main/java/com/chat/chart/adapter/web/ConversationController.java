package com.chat.chart.adapter.web;

import com.chat.chart.app.dto.ConversationDTO;
import com.chat.chart.app.dto.ConversationListResponse;
import com.chat.chart.app.dto.MessageDTO;
import com.chat.chart.app.service.ConversationAppService;
import com.chat.chart.domain.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话控制器
 * <p>
 * 提供会话（Conversation）的创建、列表查询和历史消息获取接口。
 * 每个会话对应一组连续的聊天记录，用于组织和管理多轮对话。
 * </p>
 *
 * @see ConversationAppService
 */
@RestController
@RequestMapping("/api")
public class ConversationController {

    private static final Logger log = LoggerFactory.getLogger(ConversationController.class);

    /** 会话应用服务 */
    private final ConversationAppService conversationAppService;

    /**
     * 构造方法，注入会话服务
     *
     * @param conversationAppService 会话应用服务
     */
    public ConversationController(ConversationAppService conversationAppService) {
        this.conversationAppService = conversationAppService;
    }

    /**
     * 创建新会话
     * <p>
     * 为当前用户创建一个新的会话，返回包含会话ID和创建时间等信息的会话对象。
     * </p>
     *
     * @param userId 从认证过滤器传递的用户ID
     * @return 会话信息
     */
    @PostMapping("/conversation")
    public ConversationDTO createConversation(@RequestAttribute("userId") Long userId) {
        log.info("[Conversation] 创建会话: userId={}", userId);
        return conversationAppService.createConversation(userId);
    }

    /**
     * 获取当前用户的所有会话列表
     * <p>
     * 按时间倒序返回用户的所有会话，包含每个会话的最新消息摘要。
     * </p>
     *
     * @param userId 从认证过滤器传递的用户ID
     * @return 会话列表响应
     */
    @GetMapping("/conversations")
    public ConversationListResponse listConversations(@RequestAttribute("userId") Long userId) {
        log.info("[Conversation] 列出会话: userId={}", userId);
        return conversationAppService.listConversations(userId);
    }

    /**
     * 获取指定会话的历史消息列表
     * <p>
     * 根据会话ID查询该会话下的所有聊天消息记录。
     * </p>
     *
     * @param conversationId 会话ID
     * @return 消息列表
     */
    @GetMapping("/conversation/{conversationId}/messages")
    public List<MessageDTO> getConversationMessages(@PathVariable String conversationId) {
        log.info("[Conversation] 获取消息: conversationId={}", conversationId);
        List<ChatMessage> messages = conversationAppService.getConversationMessages(conversationId);
        List<MessageDTO> dtos = new java.util.ArrayList<>();
        for (ChatMessage msg : messages) {
            dtos.add(MessageDTO.from(msg));
        }
        return dtos;
    }
}
