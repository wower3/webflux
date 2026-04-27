package com.chat.chart.domain.gateway;

/**
 * AI流式事件回调接口
 * <p>
 * 用于AI网关实现向调用方传递流式事件数据，
 * 使调用方能够在事件推送的同时进行内容累积等操作。
 * </p>
 *
 * @author Chat Chart System
 */
public interface AiStreamCallback {

    /**
     * 发送一个SSE事件数据
     *
     * @param data 事件数据（JSON字符串）
     */
    void sendEvent(String data);

    /**
     * 流正常结束
     */
    void complete();

    /**
     * 流异常结束
     *
     * @param e 异常对象
     */
    void error(Throwable e);
}
