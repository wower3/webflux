package com.chat.chart.model;

/**
 * 流式事件类型枚举
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
public enum StreamEventType {

    /**
     * 文本内容
     */
    CONTENT("content"),

    /**
     * 图表数据
     */
    CHART("chart"),

    /**
     * 卡片数据
     */
    CARD("card"),

    /**
     * 流结束标志
     */
    END("end");

    private final String value;

    StreamEventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
