package com.chat.chart.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 * <p>
 * 登录或注册成功后返回的令牌和用户名信息。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * 认证令牌，用于后续接口的身份验证
     */
    private String token;

    /**
     * 登录成功的用户名
     */
    private String username;
}
