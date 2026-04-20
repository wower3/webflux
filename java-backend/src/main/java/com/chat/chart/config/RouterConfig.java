package com.chat.chart.config;

import com.chat.chart.controller.ChatController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.ServerRequest;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * 路由配置
 * 定义所有API端点的路由规则
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ChatController chatController) {
        return route()
                // 根路径
                .GET("/", request -> chatController.root(request))

                // 健康检查
                .GET("/health", request -> chatController.health(request))

                // 流式聊天（GET/POST）
                .GET("/api/chat/stream", request -> chatController.chatStreamGet(request))
                .POST("/api/chat/stream", request -> chatController.chatStreamPost(request))

                // 非流式聊天（测试用）
                .POST("/api/chat", request -> chatController.chat(request))

                // 回显模式（GET/POST）
                .GET("/api/chat/echo/stream", request -> chatController.echoStreamGet(request))
                .POST("/api/chat/echo/stream", request -> chatController.echoStreamPost(request))

                .build();
    }
}
