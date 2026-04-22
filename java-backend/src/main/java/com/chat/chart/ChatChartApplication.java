package com.chat.chart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Chat Chart 智能对话图表系统 - 应用启动类
 * <p>
 * 基于 Spring Boot 的 WebFlux 响应式应用入口，
 * 提供流式聊天、会话管理、AI对话等功能。
 * </p>
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
@SpringBootApplication
public class ChatChartApplication {

    /**
     * 应用主入口，启动 Spring Boot 容器
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ChatChartApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("Chat Chart API is running!");
        System.out.println("API文档: http://localhost:8080/actuator");
        System.out.println("========================================\n");
    }
}
