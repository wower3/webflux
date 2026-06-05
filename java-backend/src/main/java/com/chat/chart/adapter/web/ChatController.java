package com.chat.chart.adapter.web;

import com.chat.chart.client.api.ChatAppServiceI;
import com.chat.chart.client.dto.AdoptionRequest;
import com.chat.chart.client.dto.ChatRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/chatbot")
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    private final ChatAppServiceI chatAppService;

    public ChatController(ChatAppServiceI chatAppService) {
        this.chatAppService = chatAppService;
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
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestBody ChatRequest chatRequest,
            @RequestParam("userId") Long userId) {

        LOGGER.info("流式聊天请求: message={}, userId={}", chatRequest.getMessage(), userId);

        return chatAppService.handleMessage(
                chatRequest.getMessage(),
                userId,
                chatRequest.getConversationId()
        );
    }

    @PostMapping("/datahub/old-stat/complaint")
    public String complaint(@RequestBody Map<String, Object> cardInfo) {
        LOGGER.info("投诉统计回调: cardInfo={}", cardInfo);
        return "/datahub/old-stat/complaint/2238730974744936450?id=2238730974744936450";
    }

    /**
     * 更新一轮对话的采纳状态
     *
     * @param adoptionRequest 采纳请求体，包含requestId和adoptionStatus
     * @return 操作结果
     */
    @PostMapping("/chat/adoption")
    public Map<String, Object> updateAdoptionStatus(@RequestBody AdoptionRequest adoptionRequest) {
        LOGGER.info("更新消息采纳状态: requestId={}, adoptionStatus={}",
                adoptionRequest.getRequestId(), adoptionRequest.getAdoptionStatus());
        chatAppService.updateAdoptionStatus(adoptionRequest.getRequestId(), adoptionRequest.getAdoptionStatus());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }
}
