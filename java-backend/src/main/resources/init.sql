-- ========================================
-- Chat Chart 数据库初始化脚本
-- 适用于全新环境，会先删除已有数据库再重建
-- ========================================

-- 创建数据库
DROP DATABASE IF EXISTS `chat_chart`;
CREATE DATABASE `chat_chart` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `chat_chart`;

-- 用户表
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(128) NOT NULL COMMENT '密码(SHA-256)',
    `token` VARCHAR(64) DEFAULT NULL COMMENT '认证token',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_token` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 会话表
CREATE TABLE `conversation` (
    `conversation_id` VARCHAR(64) NOT NULL COMMENT '会话ID(时间戳+随机后缀)',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`conversation_id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_conversation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 聊天消息表
CREATE TABLE `chat_message` (
    `conversation_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `request_id` VARCHAR(64) NOT NULL COMMENT '请求ID(时间戳+随机后缀, 一轮对话中user和assistant共用)',
    `session_id` VARCHAR(64) DEFAULT NULL COMMENT '会话轮次ID(预留)',
    `role` VARCHAR(20) NOT NULL COMMENT '角色(user/assistant)',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`request_id`, `role`),
    KEY `idx_conversation` (`conversation_id`, `created_at`),
    KEY `idx_request_id_created` (`request_id`, `created_at`),
    CONSTRAINT `fk_message_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`conversation_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 初始用户: abc / 123456
INSERT INTO `user` (`username`, `password`) VALUES ('abc', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
