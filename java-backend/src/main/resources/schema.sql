-- Chat Chart 数据库初始化脚本

CREATE TABLE IF NOT EXISTS ``user`` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(128) NOT NULL COMMENT '密码(SHA-256)',
    `token` VARCHAR(64) DEFAULT NULL COMMENT '认证token',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_token` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `conversation` (
    `conversation_id` VARCHAR(64) NOT NULL COMMENT '会话ID(UUID)',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`conversation_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

CREATE TABLE IF NOT EXISTS `chat_message` (
    `conversation_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `request_id` VARCHAR(64) NOT NULL COMMENT '请求ID(时间戳+随机后缀)',
    `session_id` VARCHAR(64) DEFAULT NULL COMMENT '会话轮次ID(预留)',
    `role` VARCHAR(20) NOT NULL COMMENT '角色(user/assistant)',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`request_id`, `role`),
    KEY `idx_conversation` (`conversation_id`, `created_at`),
    KEY `idx_request_id_created` (`request_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';
