package com.chat.chart.domain.model;

/**
 * 采纳状态枚举
 * <p>
 * 表示一轮对话（一问一答）的采纳与否。
 * </p>
 */
public enum AdoptionStatus {

    NOT_ADOPTED("0"),
    ADOPTED("1"),
    DEFAULT("2");

    private final String value;

    AdoptionStatus(String value) {
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
