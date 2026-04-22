package com.chat.chart.app.dto;

import lombok.Data;

/**
 * 登录请求DTO
 * <p>
 * 封装用户登录所需的用户名和密码参数。
 * </p>
 */
@Data
public class LoginRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;
}
