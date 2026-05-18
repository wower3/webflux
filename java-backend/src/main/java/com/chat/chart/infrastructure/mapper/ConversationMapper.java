package com.chat.chart.infrastructure.mapper;

import com.chat.chart.infrastructure.dataobject.ConversationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话数据访问接口
 */
@Mapper
public interface ConversationMapper {

    /**
     * 根据用户ID查询最新会话
     *
     * @param userId 用户ID
     * @return 最新会话数据对象
     */
    ConversationDO selectLatestByUserId(@Param("userId") Long userId);

    /**
     * 查询用户所有会话
     *
     * @param userId 用户ID
     * @return 会话数据对象列表
     */
    List<ConversationDO> selectByUserId(@Param("userId") Long userId);

    /**
     * 新建会话
     *
     * @param conversation 会话数据对象
     * @return 影响行数
     */
    int insert(ConversationDO conversation);

    /**
     * 统计会话消息数
     *
     * @param conversationId 会话ID
     * @return 消息数量
     */
    int countMessagesByConversationId(@Param("conversationId") String conversationId);

    /**
     * 根据会话ID和用户ID查询会话
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @return 会话数据对象
     */
    ConversationDO selectByConversationIdAndUserId(@Param("conversationId") String conversationId, @Param("userId") Long userId);
}
