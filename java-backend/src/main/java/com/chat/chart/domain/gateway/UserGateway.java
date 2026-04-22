package com.chat.chart.domain.gateway;

import com.chat.chart.domain.model.User;

/**
 * 用户网关接口
 * <p>
 * 定义用户的持久化操作，包括用户查询、注册与token管理。
 * </p>
 *
 * @author Chat Chart System
 */
public interface UserGateway {

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息，不存在返回null
     */
    User findByUsername(String username);

    /**
     * 根据token查找用户
     *
     * @param token 认证token
     * @return 用户信息，不存在返回null
     */
    User findByToken(String token);

    /**
     * 保存新用户
     *
     * @param username 用户名
     * @param password 密码（已哈希）
     */
    void saveUser(String username, String password);

    /**
     * 更新用户token
     *
     * @param userId 用户ID
     * @param token   新token
     */
    void updateToken(Long userId, String token);
}
