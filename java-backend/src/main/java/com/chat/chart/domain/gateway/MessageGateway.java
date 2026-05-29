package com.chat.chart.domain.gateway;

import com.chat.chart.domain.model.ChatMessage;

import java.util.List;

/**
 * 消息网关接口
 * <p>
 * 定义聊天消息的持久化操作，包括消息保存与上下文查询。
 * </p>
 *
 * @author Chat Chart System
 */
public interface MessageGateway {

    /**
     * 保存消息
     *
     * @param requestId      请求ID（一轮对话中 user 和 assistant 共用）
     * @param conversationId 所属会话ID
     * @param role           消息角色（user / assistant）
     * @param content        消息内容
     * @param isSuccess      调用是否成功（1-成功，0-失败，null-用户消息）
     */
    void saveMessage(String requestId, String conversationId, String role, String content, String isSuccess);

    /**
     * 查询上下文消息
     * <p>
     * 在指定会话中，查找最近若干个请求轮次（request）的所有消息，
     * 用于拼接发送给AI模型的上下文。
     * </p>
     *
     * @param conversationId 会话ID
     * @param maxRequests    最大查询的请求轮次数
     * @return 按时间正序排列的消息列表
     */
    List<ChatMessage> findContextMessages(String conversationId, int maxRequests);

    /**
     * 更新一轮对话的采纳状态
     *
     * @param requestId      请求ID（一轮对话的标识）
     * @param adoptionStatus 采纳状态（0-未采纳，1-已采纳，2-默认）
     */
    void updateAdoptionStatus(String requestId, String adoptionStatus);
}
