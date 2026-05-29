package com.chat.chart.infrastructure.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 统计数据访问接口
 */
@Mapper
public interface StatMapper {

    /**
     * 统计用户量（按conversation_id去重）
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 用户量
     */
    int selectUserCount(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计交易量（模型回复数）
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 交易量
     */
    int selectTransactionCount(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计未采纳数
     *
     * @param startTime         开始时间
     * @param endTime           结束时间
     * @param notAdoptedStatus  未采纳状态值
     * @return 未采纳数
     */
    int selectNotAdoptedCount(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("notAdoptedStatus") String notAdoptedStatus);
}
