package com.chat.chart.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户领域模型
 * <p>
 * 表示系统中的注册用户，包含认证信息和token。
 * </p>
 *
 * @author Chat Chart System
 */
@Data
public class User {

    /** 用户主键ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码（SHA-256哈希存储） */
    private String password;

    /** 认证token */
    private String token;

    /** 用户创建时间 */
    private LocalDateTime createdAt;
}
