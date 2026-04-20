package com.chat.chart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Chat Chart 应用启动类
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
@SpringBootApplication
public class ChatChartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatChartApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("Chat Chart API is running!");
        System.out.println("API文档: http://localhost:8080/actuator");
        System.out.println("========================================\n");
    }
}
