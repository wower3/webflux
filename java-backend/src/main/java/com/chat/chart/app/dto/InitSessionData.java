package com.chat.chart.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 初始请求DTO
 *
 * @author Chat Chart System
 * @version 1.0.0
 */

@Builder
@Data
public class InitSessionData {
    @JsonProperty("config_variables")
    private List<ConfigVariable> configVariables;
}

