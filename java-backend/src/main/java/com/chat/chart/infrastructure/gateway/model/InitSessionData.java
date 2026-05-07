package com.chat.chart.infrastructure.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * AI服务会话初始化请求数据
 */
@Builder
@Data
public class InitSessionData {
    @JsonProperty("config_variables")
    private List<ConfigVariable> configVariables;
}
