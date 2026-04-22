package com.chat.chart.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * SSE流式事件模型
 * <p>
 * 定义SSE流中每个事件的结构，包含事件类型和数据内容。
 * 通过静态工厂方法快速创建内容事件和结束事件。
 * </p>
 */
@Data
public class StreamEvent {

    /**
     * 事件类型，对应 {@link StreamEventType} 的值
     */
    @JsonProperty("type")
    private String type;

    /**
     * 事件携带的数据内容
     */
    @JsonProperty("data")
    private Object data;

    /**
     * 创建文本内容事件
     *
     * @param content 文本内容
     * @return 类型为content的流式事件
     */
    public static StreamEvent content(String content) {
        StreamEvent event = new StreamEvent();
        event.setType(StreamEventType.CONTENT.getValue());
        event.setData(content);
        return event;
    }

    /**
     * 创建流结束事件
     *
     * @return 类型为end的流式事件，数据为null
     */
    public static StreamEvent end() {
        StreamEvent event = new StreamEvent();
        event.setType(StreamEventType.END.getValue());
        event.setData(null);
        return event;
    }
}
