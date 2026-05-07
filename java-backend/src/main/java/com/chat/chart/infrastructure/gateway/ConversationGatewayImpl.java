package com.chat.chart.infrastructure.gateway;

import com.chat.chart.domain.gateway.ConversationGateway;
import com.chat.chart.domain.model.ChatMessage;
import com.chat.chart.domain.model.Conversation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 会话网关实现
 * <p>
 * 基于JdbcTemplate操作数据库，实现会话的查询与持久化。
 * </p>
 *
 * @author Chat Chart System
 */
@Repository
public class ConversationGatewayImpl implements ConversationGateway {

    private static final Logger log = LoggerFactory.getLogger(ConversationGatewayImpl.class);

    /** JDBC模板 */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 构造函数
     *
     * @param jdbcTemplate JDBC模板
     */
    public ConversationGatewayImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 查找用户最新的会话
     * <p>
     * 按创建时间倒序取第一条记录，查询异常时返回null。
     * </p>
     *
     * @param userId 用户ID
     * @return 最新会话，不存在返回null
     */
    @Override
    public Conversation findLatestByUserId(Long userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT conversation_id, user_id, created_at FROM conversation WHERE user_id = ? ORDER BY created_at DESC LIMIT 1",
                    new Object[]{userId},
                    new ConversationRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            log.debug("[ConversationGateway] 用户无会话: userId={}", userId);
            return null;
        }
    }

    /**
     * 查找用户所有会话
     *
     * @param userId 用户ID
     * @return 会话列表，按创建时间倒序排列
     */
    @Override
    public List<Conversation> findByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT conversation_id, user_id, created_at FROM conversation WHERE user_id = ? ORDER BY created_at DESC",
                new Object[]{userId},
                new ConversationRowMapper()
        );
    }

    /**
     * 保存新会话
     *
     * @param conversationId 会话唯一标识
     * @param userId         所属用户ID
     */
    @Override
    public void saveConversation(String conversationId, Long userId) {
        jdbcTemplate.update(
                "INSERT INTO conversation (conversation_id, user_id, created_at) VALUES (?, ?, NOW())",
                conversationId, userId
        );
    }

    @Override
    public int countMessagesByConversationId(String conversationId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chat_message WHERE conversation_id = ?",
                new Object[]{conversationId},
                Integer.class
        );
        return count != null ? count : 0;
    }

    @Override
    public List<ChatMessage> findMessagesByConversationId(String conversationId) {
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
    }

    @Override
    public Conversation findByConversationIdAndUserId(String conversationId, Long userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT conversation_id, user_id, created_at FROM conversation WHERE conversation_id = ? AND user_id = ?",
                    new Object[]{conversationId, userId},
                    new ConversationRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 会话结果集映射器
     * <p>
     * 将数据库查询结果映射为 {@link Conversation} 对象。
     * </p>
     */
    private static class ConversationRowMapper implements RowMapper<Conversation> {
        @Override
        public Conversation mapRow(ResultSet rs, int rowNum) throws SQLException {
            Conversation conv = new Conversation();
            conv.setConversationId(rs.getString("conversation_id"));
            conv.setUserId(rs.getLong("user_id"));
            conv.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return conv;
        }
    }
}
