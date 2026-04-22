package com.chat.chart.infrastructure.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent协议基础请求体
 * <p>
 * 封装与外部AI服务通信的统一请求格式，
 * 包含协议版本、交易码、请求标识等公共字段。
 * </p>
 *
 * @param <T> 业务数据类型
 * @author Chat Chart System
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseAgentRequest<T> {

    /** 应用标识 */
    @JsonProperty("appId")
    private String appId;

    /** 交易码，标识具体的业务接口 */
    @JsonProperty("trCode")
    private String trCode;

    /** 协议版本号 */
    @JsonProperty("trVersion")
    private String trVersion;

    /** 请求时间戳 */
    @JsonProperty("timestamp")
    private String timestamp;

    /** 请求唯一标识，用于链路追踪 */
    @JsonProperty("requestId")
    private String requestId;

    /** 业务数据载荷 */
    @JsonProperty("data")
    private T data;
}
