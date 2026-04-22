package com.chat.chart.infrastructure.config;

import org.springframework.context.annotation.Configuration;

/**
 * 数据源配置类
 * <p>
 * 当前使用 Spring Boot 自动配置机制，根据 application.yml 中的
 * spring.r2dbc 相关配置自动创建数据源连接。
 * 后续如需自定义数据源（如连接池参数、多数据源等），可在此类中扩展配置。
 * </p>
 */
@Configuration
public class DataSourceConfig {
    // 占位类，数据源由 Spring Boot 根据 application.yml 自动配置
}
