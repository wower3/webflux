package com.chat.chart.infrastructure.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * LLM参数表数据访问接口
 */
@Mapper
public interface LlmParameterMapper {

    /**
     * 根据key查询value
     *
     * @param llmKey 参数key
     * @return 参数value
     */
    String selectValueByKey(@Param("llmKey") String llmKey);

    /**
     * 乐观锁更新（CAS）
     *
     * @param llmKey  参数key
     * @param oldValue 旧值
     * @param newValue 新值
     * @return 影响行数，0表示已被其他pod修改
     */
    int casUpdate(@Param("llmKey") String llmKey, @Param("oldValue") String oldValue, @Param("newValue") String newValue);

    /**
     * 插入初始数据（仅当key不存在时插入）
     *
     * @param llmKey  参数key
     * @param llmValue 参数value
     * @return 影响行数，0表示key已存在
     */
    int insertIfAbsent(@Param("llmKey") String llmKey, @Param("llmValue") String llmValue);
}
