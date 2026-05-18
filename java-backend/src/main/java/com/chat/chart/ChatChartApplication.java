package com.chat.chart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Chat Chart 智能对话图表系统 - 应用启动类
 * <p>
 * 基于 Spring Boot 的 Web 应用入口，
 * 提供流式聊天、会话管理、AI对话等功能。
 * </p>
 */
@SpringBootApplication
@MapperScan("com.chat.chart.infrastructure.mapper")
public class ChatChartApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatChartApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ChatChartApplication.class, args);
        LOGGER.info("Chat Chart API is running!");
    }
}
