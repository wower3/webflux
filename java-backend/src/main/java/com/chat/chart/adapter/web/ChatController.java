package com.chat.chart.adapter.web;

import com.chat.chart.app.dto.ChatRequest;
import com.chat.chart.app.service.ChatAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天控制器
 * <p>
 * 提供聊天相关的API接口，包括根路径信息、健康检查和流式聊天（SSE）。
 * </p>
 *
 * @see ChatAppService
 */
@RestController
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    /** 聊天应用服务，处理消息并生成AI响应 */
    private final ChatAppService chatAppService;

    /**
     * 构造方法，注入聊天服务
     *
     * @param chatAppService 聊天应用服务
     */
    public ChatController(ChatAppService chatAppService) {
        this.chatAppService = chatAppService;
    }

    /**
     * 根路径，返回API服务基本信息
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

    /**
     * 流式聊天接口（SSE）
     * <p>
     * 接收用户消息，以Server-Sent Events流式返回AI响应内容，
     * 支持实时推送文本和图表数据。
     * </p>
     *
     * @param chatRequest 聊天请求体，包含消息内容和会话ID
     * @param userId      从认证过滤器传递的用户ID
     * @return SseEmitter，用于流式推送AI响应
     */
    @PostMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestBody ChatRequest chatRequest,
            @RequestAttribute("userId") Long userId) {

        log.info("[POST /api/chat/stream] message={}, userId={}", chatRequest.getMessage(), userId);

        return chatAppService.handleMessage(
                chatRequest.getMessage(),
                userId,
                chatRequest.getConversationId()
        );
    }

    @PostMapping("/api/datahub/old-stat/complaint")
    public String complaint(@RequestBody Map<String, Object> cardInfo) {
        log.info("[POST /api/datahub/old-stat/complaint] cardInfo={}", cardInfo);
        return "/datahub/old-stat/complaint/2238730974744936450?id=2238730974744936450";
    }
}
