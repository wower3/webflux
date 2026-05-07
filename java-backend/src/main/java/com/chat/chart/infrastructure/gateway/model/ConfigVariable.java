package com.chat.chart.infrastructure.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI服务配置变量
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigVariable {
    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;
}
