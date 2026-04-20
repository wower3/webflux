package com.chat.chart.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 图表数据模型
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartData {

    /**
     * 图表唯一ID
     */
    @JsonProperty("chartId")
    private String chartId;

    /**
     * 类型标识（固定为"chart"）
     */
    @JsonProperty("type")
    private String type;

    /**
     * 图表子类型: line | bar | pie | scatter
     */
    @JsonProperty("subtype")
    private String subtype;

    /**
     * 图表标题
     */
    @JsonProperty("title")
    private String title;

    /**
     * 图表数据
     */
    @JsonProperty("data")
    private Map<String, Object> data;
}
