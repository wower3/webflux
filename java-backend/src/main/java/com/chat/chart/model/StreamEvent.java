package com.chat.chart.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 流式事件模型
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
@Data
public class StreamEvent {

    /**
     * 事件类型: content | chart | card | end
     */
    @JsonProperty("type")
    private String type;

    /**
     * 事件数据
     */
    @JsonProperty("data")
    private Object data;

    /**
     * 创建内容事件
     */
    public static StreamEvent content(String content) {
        StreamEvent event = new StreamEvent();
        event.setType(StreamEventType.CONTENT.getValue());
        event.setData(content);
        return event;
    }

    /**
     * 创建图表事件
     */
    public static StreamEvent chart(ChartData chartData) {
        StreamEvent event = new StreamEvent();
        event.setType(StreamEventType.CHART.getValue());
        event.setData(chartData);
        return event;
    }

    /**
     * 创建卡片事件
     */
    public static StreamEvent card(CardData cardData) {
        StreamEvent event = new StreamEvent();
        event.setType(StreamEventType.CARD.getValue());
        event.setData(cardData);
        return event;
    }

    /**
     * 创建结束事件
     */
    public static StreamEvent end() {
        StreamEvent event = new StreamEvent();
        event.setType(StreamEventType.END.getValue());
        event.setData(null);
        return event;
    }
}
