package com.chat.chart.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 卡片信息项模型
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardInfoItem {

    /**
     * 信息键
     */
    @JsonProperty("key")
    private String key;

    /**
     * 显示标签
     */
    @JsonProperty("label")
    private String label;

    /**
     * 信息值
     */
    @JsonProperty("value")
    private String value;
}
