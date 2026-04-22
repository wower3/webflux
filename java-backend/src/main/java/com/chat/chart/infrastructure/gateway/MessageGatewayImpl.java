package com.chat.chart.infrastructure.gateway;

import com.chat.chart.domain.gateway.MessageGateway;
import com.chat.chart.domain.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
        // 步骤1：查找最近maxRequests个不同的requestId
        String requestSql = "SELECT request_id FROM chat_message WHERE conversation_id = ? GROUP BY request_id ORDER BY MAX(created_at) DESC LIMIT ?";
        List<String> requests = jdbcTemplate.queryForList(requestSql, new Object[]{conversationId, maxRequests}, String.class);

        if (requests.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 步骤2：构建IN查询占位符
        StringBuilder placeholders = new StringBuilder();
        Object[] params = new Object[requests.size()];
        for (int i = 0; i < requests.size(); i++) {
            if (i > 0) {
                placeholders.append(",");
            }
            placeholders.append("?");
            params[i] = requests.get(i);
        }

        // 步骤3：查询这些request下的所有消息，按时间正序排列
        return jdbcTemplate.query(
                "SELECT request_id, conversation_id, role, content, created_at FROM chat_message WHERE conversation_id = ? AND request_id IN ("
                        + placeholders.toString()
                        + ") ORDER BY created_at ASC",
                concatenateParams(new Object[]{conversationId}, params),
                new MessageRowMapper()
        );
    }

    /**
     * 合并两个参数数组
     *
     * @param first  第一个参数数组
     * @param second 第二个参数数组
     * @return 合并后的新数组
     */
    private Object[] concatenateParams(Object[] first, Object[] second) {
        Object[] result = new Object[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
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
