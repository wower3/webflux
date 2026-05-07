package com.chat.chart.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * SSE流式事件模型
 */
@Data
public class StreamEvent {

    @JsonProperty("type")
    private String type;

    @JsonProperty("data")
    private Object data;

    public static StreamEvent content(String content) {
        StreamEvent event = new StreamEvent();
        event.setType(StreamEventType.CONTENT.getValue());
        event.setData(content);
        return event;
    }

    public static StreamEvent end() {
        StreamEvent event = new StreamEvent();
        event.setType(StreamEventType.END.getValue());
        event.setData(null);
        return event;
    }
}
