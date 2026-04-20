package com.chat.chart.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 卡片按钮模型
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardButton {

    /**
     * 按钮动作ID
     */
    @JsonProperty("actionId")
    private String actionId;

    /**
     * 按钮标签文本
     */
    @JsonProperty("label")
    private String label;

    /**
     * API端点（可选）
     */
    @JsonProperty("apiEndpoint")
    private String apiEndpoint;
}
