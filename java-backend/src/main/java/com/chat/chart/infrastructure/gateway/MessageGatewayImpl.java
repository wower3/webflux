package com.chat.chart.infrastructure.gateway;

import com.chat.chart.domain.gateway.MessageGateway;
import com.chat.chart.domain.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 消息网关实现
 * <p>
 * 基于JdbcTemplate操作数据库，实现消息的保存与上下文查询。
 * </p>
 *
 * @author Chat Chart System
 */
@Repository
public class MessageGatewayImpl implements MessageGateway {

    private static final Logger log = LoggerFactory.getLogger(MessageGatewayImpl.class);

    /** JDBC模板 */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 构造函数
     *
     * @param jdbcTemplate JDBC模板
     */
    public MessageGatewayImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 保存消息
     *
     * @param requestId      请求ID（一轮对话中 user 和 assistant 共用）
     * @param conversationId 所属会话ID
     * @param role           消息角色
     * @param content        消息内容
     */
    @Override
    public void saveMessage(String requestId, String conversationId, String role, String content) {
        jdbcTemplate.update(
                "INSERT INTO chat_message (request_id, conversation_id, role, content, created_at) VALUES (?, ?, ?, ?, NOW())",
                requestId, conversationId, role, content
        );
    }

    /**
     * 查询上下文消息
     * <p>
     * 先查找最近 {@code maxRequests} 个不同的请求轮次（request_id），
     * 再获取这些轮次中的全部消息，用于拼接AI上下文。
     * </p>
     *
     * @param conversationId 会话ID
     * @param maxRequests    最大查询的请求轮次数
     * @return 按时间正序排列的消息列表
     */
    @Override
    public List<ChatMessage> findContextMessages(String conversationId, int maxRequests) {
        String requestSql = "SELECT request_id FROM chat_message WHERE conversation_id = :conversationId GROUP BY request_id ORDER BY MAX(created_at) DESC LIMIT :maxRequests";
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("conversationId", conversationId);
        params.put("maxRequests", maxRequests);
        List<String> requests = new NamedParameterJdbcTemplate(jdbcTemplate).queryForList(requestSql, params, String.class);

        if (requests.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        Map<String, Object> queryParams = new java.util.HashMap<>();
        queryParams.put("conversationId", conversationId);
        queryParams.put("requestIds", requests);
        return new NamedParameterJdbcTemplate(jdbcTemplate).query(
                "SELECT request_id, conversation_id, role, content, created_at FROM chat_message WHERE conversation_id = :conversationId AND request_id IN (:requestIds) ORDER BY created_at ASC",
                queryParams,
                new MessageRowMapper()
        );
    }

    /**
     * 消息结果集映射器
     * <p>
     * 将数据库查询结果映射为 {@link ChatMessage} 对象。
     * </p>
     */
    private static class MessageRowMapper implements RowMapper<ChatMessage> {
        @Override
        public ChatMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatMessage msg = new ChatMessage();
            msg.setRequestId(rs.getString("request_id"));
            msg.setConversationId(rs.getString("conversation_id"));
            msg.setRole(rs.getString("role"));
            msg.setContent(rs.getString("content"));
            msg.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return msg;
        }
    }
}
