package com.chat.chart.adapter.web;

import com.chat.chart.client.api.ChatAppServiceI;
import com.chat.chart.client.dto.AdoptionRequest;
import com.chat.chart.client.dto.ChatRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天控制器
 *
 * @see ChatAppServiceI
 */
@RestController
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    private final ChatAppServiceI chatAppService;

    public ChatController(ChatAppServiceI chatAppService) {
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
            @RequestParam("userId") Long userId) {

        LOGGER.info("[POST /api/chat/stream] message={}, userId={}", chatRequest.getMessage(), userId);

        return chatAppService.handleMessage(
                chatRequest.getMessage(),
                userId,
                chatRequest.getConversationId()
        );
    }

    @PostMapping("/api/datahub/old-stat/complaint")
    public String complaint(@RequestBody Map<String, Object> cardInfo) {
        LOGGER.info("[POST /api/datahub/old-stat/complaint] cardInfo={}", cardInfo);
        return "/datahub/old-stat/complaint/2238730974744936450?id=2238730974744936450";
    }

    /**
     * 更新一轮对话的采纳状态
     *
     * @param adoptionRequest 采纳请求体，包含requestId和adoptionStatus
     * @return 操作结果
     */
    @PostMapping("/api/chat/adoption")
    public Map<String, Object> updateAdoptionStatus(@RequestBody AdoptionRequest adoptionRequest) {
        LOGGER.info("[POST /api/chat/adoption] requestId={}, adoptionStatus={}",
                adoptionRequest.getRequestId(), adoptionRequest.getAdoptionStatus());
        chatAppService.updateAdoptionStatus(adoptionRequest.getRequestId(), adoptionRequest.getAdoptionStatus());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }
}
