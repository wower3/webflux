package com.chat.chart.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步线程池配置
 * <p>
 * 配置聊天业务专用的线程池，用于SSE流式接口的异步执行，
 * 避免阻塞Tomcat Servlet线程。
 * </p>
 *
 * @author Chat Chart System
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 聊天业务专用线程池
     *
     * @return Executor 实例
     */
    @Bean("chatExecutor")
    public Executor chatExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("chat-");
        executor.initialize();
        return executor;
    }
}
