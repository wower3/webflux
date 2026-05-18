package com.chat.chart.infrastructure.mapper;

import com.chat.chart.infrastructure.dataobject.ChatMessageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息数据访问接口
 */
@Mapper
public interface ChatMessageMapper {

    /**
     * 插入消息
     *
     * @param message 消息数据对象
     * @return 影响行数
     */
    int insert(ChatMessageDO message);

    /**
     * 查询最近N个requestId
     *
     * @param conversationId 会话ID
     * @param maxRequests    最大请求数
     * @return requestId列表
     */
    List<String> selectRecentRequestIds(@Param("conversationId") String conversationId, @Param("maxRequests") int maxRequests);

    /**
     * 根据conversationId和requestIds查询消息
     *
     * @param conversationId 会话ID
     * @param requestIds     请求ID列表
     * @return 消息数据对象列表
     */
    List<ChatMessageDO> selectByIds(@Param("conversationId") String conversationId, @Param("requestIds") List<String> requestIds);

    /**
     * 根据会话ID查询所有消息
     *
     * @param conversationId 会话ID
     * @return 消息数据对象列表
     */
    List<ChatMessageDO> selectByConversationId(@Param("conversationId") String conversationId);
}
