package com.chat.chart.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI服务配置属性
 * <p>
 * 从 application.yml 中读取 ai-service 前缀下的配置项，
 * 用于配置外部AI服务的连接地址、接口路径和超时时间。
 * </p>
 * <p>
 * 配置示例（application.yml）：
 * <pre>
 * ai-service:
 *   url: http://aiml-pub.aisp.test.abc/agent-api/workflow-agent-1-a852be77
 *   chat-api: /chatabc/chat
 *   timeout: 90000
 * </pre>
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai-service")
public class AiServiceProperties {

    /** AI服务的基础URL地址 */
    private String url = "http://localhost:9999";

     /** AI初始化接口的路径后缀，拼接在url之后 */
     private String initApi = "/chatabc/init_session";
    
    /** AI聊天接口的路径后缀，拼接在url之后 */
    private String chatApi = "/chatabc/chat";

    /** 请求超时时间（毫秒），默认90秒 */
    private long timeout = 90000L;
}
