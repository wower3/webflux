package com.chat.chart.adapter.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * <p>
 * 独立于 /chatbot 前缀，不受 JWT 拦截器影响。
 * </p>
 */
@RestController
public class HealthController {

    /**
     * 根路径，返回服务基本信息
     *
     * @return 包含服务名称和版本号的Map
     */
    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Chat Chart API is running");
        result.put("version", "1.0.0");
        return result;
    }

    /**
     * 健康检查接口
     *
     * @return 包含健康状态的Map
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "healthy");
        return result;
    }
}
