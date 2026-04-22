package com.chat.chart.app.dto;

/**
 * 流式事件类型枚举
 * <p>
 * 定义SSE流中支持的所有事件类型，前端根据type字段进行不同处理。
 * </p>
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
public enum StreamEventType {

    /**
     * 文本内容事件 - 用于流式传输AI回复的文本片段
     */
    CONTENT("content"),

    /**
     * 图表数据事件 - 携带ECharts图表的完整JSON配置
     */
    CHART("chart"),

    /**
     * 卡片数据事件 - 携带结构化卡片展示数据
     */
    CARD("card"),

    /**
     * 流结束标志事件 - 通知客户端SSE流已结束
     */
    END("end");

    /**
     * 事件类型的字符串值，对应SSE中的type字段
     */
    private final String value;

    /**
     * 构造函数
     *
     * @param value 事件类型的字符串值
     */
    StreamEventType(String value) {
        this.value = value;
    }

    /**
     * 获取事件类型的字符串值
     *
     * @return 类型字符串
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
