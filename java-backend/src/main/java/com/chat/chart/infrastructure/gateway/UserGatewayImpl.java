package com.chat.chart.infrastructure.gateway;

import com.chat.chart.domain.gateway.UserGateway;
import com.chat.chart.domain.model.User;
import com.chat.chart.domain.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * 用户网关实现
 * <p>
 * 基于JdbcTemplate操作数据库，实现用户的查询、注册与token管理。
 * 密码采用SHA-256哈希存储。
 * </p>
 *
 * @author Chat Chart System
 */
@Repository
public class UserGatewayImpl implements UserGateway {

    private static final Logger log = LoggerFactory.getLogger(UserGatewayImpl.class);

    /** JDBC模板 */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 构造函数
     *
     * @param jdbcTemplate JDBC模板
     */
    public UserGatewayImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 根据用户名查找用户
     * <p>
     * 查询异常时返回null，表示用户不存在。
     * </p>
     *
     * @param username 用户名
     * @return 用户信息，不存在返回null
     */
    @Override
    public User findByUsername(String username) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id, username, password, token, created_at FROM `user` WHERE username = ?",
                    new Object[]{username},
                    new UserRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            log.debug("[UserGateway] 用户不存在: {}", username);
            return null;
        }
    }

    /**
     * 根据token查找用户
     *
     * @param token 认证token
     * @return 用户信息，不存在返回null
     */
    @Override
    public User findByToken(String token) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id, username, password, token, created_at FROM `user` WHERE token = ?",
                    new Object[]{token},
                    new UserRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            log.debug("[UserGateway] token无效: {}...", token != null && token.length() > 8 ? token.substring(0, 8) : token);
            return null;
        }
    }

    /**
     * 保存新用户
     * <p>
     * 密码在保存前会进行SHA-256哈希处理。
     * </p>
     *
     * @param username 用户名
     * @param password 密码明文
     */
    @Override
    public void saveUser(String username, String password) {
        String hashedPassword = HashUtil.sha256(password);
        jdbcTemplate.update(
                "INSERT INTO `user` (username, password, created_at) VALUES (?, ?, ?)",
                username, hashedPassword, LocalDateTime.now()
        );
    }

    /**
     * 更新用户token
     *
     * @param userId 用户ID
     * @param token   新token
     */
    @Override
    public void updateToken(Long userId, String token) {
        jdbcTemplate.update(
                "UPDATE `user` SET token = ? WHERE id = ?",
                token, userId
        );
    }

    /**
     * 用户结果集映射器
     * <p>
     * 将数据库查询结果映射为 {@link User} 对象。
     * </p>
     */
    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setToken(rs.getString("token"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return user;
        }
    }
}
