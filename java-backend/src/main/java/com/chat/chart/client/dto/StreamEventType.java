package com.chat.chart.client.dto;

/**
 * 流式事件类型枚举
 */
public enum StreamEventType {

    CONTENT("content"),
    CHART("chart"),
    CARD("card"),
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
