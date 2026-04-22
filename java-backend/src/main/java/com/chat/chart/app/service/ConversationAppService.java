package com.chat.chart.app.service;

import com.chat.chart.app.dto.ConversationDTO;
import com.chat.chart.app.dto.ConversationListResponse;
import com.chat.chart.domain.gateway.ConversationGateway;
import com.chat.chart.domain.gateway.MessageGateway;
import com.chat.chart.domain.model.ChatMessage;
import com.chat.chart.domain.model.Conversation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.chat.chart.infrastructure.util.IdGenerator;

/**
 * 会话应用服务
 * <p>
 * 提供会话的创建、列表查询和消息历史获取功能。
 * 通过 {@link ConversationGateway} 管理会话生命周期，
 * 使用 {@link JdbcTemplate} 直接查询消息统计和历史数据。
 * </p>
 */
@Service
public class ConversationAppService {

    private static final Logger log = LoggerFactory.getLogger(ConversationAppService.class);

    /**
     * 会话数据网关
     */
    private final ConversationGateway conversationGateway;

    /**
     * JDBC模板，用于直接执行消息相关的SQL查询
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 构造函数注入
     *
     * @param conversationGateway 会话数据网关
     * @param jdbcTemplate        JDBC模板
     */
    public ConversationAppService(ConversationGateway conversationGateway, JdbcTemplate jdbcTemplate) {
        this.conversationGateway = conversationGateway;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 创建新会话
     * <p>
     * 生成UUID作为会话ID，保存到数据库并返回会话DTO。
     * 新创建的会话消息数为0，状态为活跃。
     * </p>
     *
     * @param userId 所属用户ID
     * @return 新创建的会话DTO
     */
    public Mono<ConversationDTO> createConversation(Long userId) {
        return Mono.fromCallable(() -> {
            String conversationId = IdGenerator.newConversationId();
            conversationGateway.saveConversation(conversationId, userId);
            log.info("[Conversation] 创建会话: userId={}, conversationId={}", userId, conversationId);

            ConversationDTO dto = new ConversationDTO();
            dto.setConversationId(conversationId);
            dto.setCreatedAt(LocalDateTime.now());
            dto.setMessageCount(0);
            dto.setActive(true);
            return dto;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 列出用户所有会话
     * <p>
     * 查询指定用户的所有会话，并统计每个会话的消息数量，
     * 返回包含会话摘要信息的列表。
     * </p>
     *
     * @param userId 用户ID
     * @return 会话列表响应，包含所有会话的DTO列表
     */
    public Mono<ConversationListResponse> listConversations(Long userId) {
        return Mono.fromCallable(() -> {
            List<Conversation> conversations = conversationGateway.findByUserId(userId);
            List<ConversationDTO> dtos = new ArrayList<>();

            // 为每个会话查询消息数量，组装DTO
            for (Conversation conv : conversations) {
                ConversationDTO dto = new ConversationDTO();
                dto.setConversationId(conv.getConversationId());
                dto.setCreatedAt(conv.getCreatedAt());

                // 统计该会话下的消息总数
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM chat_message WHERE conversation_id = ?",
                        new Object[]{conv.getConversationId()},
                        Integer.class
                );
                dto.setMessageCount(count != null ? count : 0);
                dto.setActive(true);

                dtos.add(dto);
            }

            return new ConversationListResponse(dtos);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 获取会话消息列表
     * <p>
     * 查询指定会话的所有消息，按创建时间正序排列。
     * 用于前端加载会话历史记录。
     * </p>
     *
     * @param conversationId 会话ID
     * @return 该会话下的消息列表（按时间正序）
     */
    public Mono<List<ChatMessage>> getConversationMessages(String conversationId) {
        return Mono.fromCallable(() -> {
            return jdbcTemplate.query(
                    "SELECT request_id, conversation_id, role, content, created_at FROM chat_message WHERE conversation_id = ? ORDER BY created_at ASC",
                    new Object[]{conversationId},
                    (rs, rowNum) -> {
                        ChatMessage msg = new ChatMessage();
                        msg.setRequestId(rs.getString("request_id"));
                        msg.setConversationId(rs.getString("conversation_id"));
                        msg.setRole(rs.getString("role"));
                        msg.setContent(rs.getString("content"));
                        msg.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        return msg;
                    }
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
