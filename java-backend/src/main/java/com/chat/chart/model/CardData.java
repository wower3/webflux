package com.chat.chart.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 卡片数据模型
 *
 * @author Chat Chart System
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardData {

    /**
     * 类型标识（固定为"card"）
     */
    @JsonProperty("type")
    private String type;

    /**
     * 卡片唯一ID
     */
    @JsonProperty("cardId")
    private String cardId;

    /**
     * 卡片名称
     */
    @JsonProperty("cardName")
    private String cardName;

    /**
     * 显示标题
     */
    @JsonProperty("displayTitle")
    private String displayTitle;

    /**
     * 卡片信息列表
     */
    @JsonProperty("cardInfo")
    private List<CardInfoItem> cardInfo;

    /**
     * 按钮列表
     */
    @JsonProperty("buttons")
    private List<CardButton> buttons;

    /**
     * 创建卡片数据的便捷方法
     */
    public static CardData create(String cardId, String cardName, String displayTitle,
                                   List<CardInfoItem> cardInfo, List<CardButton> buttons) {
        return CardData.builder()
                .type("card")
                .cardId(cardId)
                .cardName(cardName)
                .displayTitle(displayTitle)
                .cardInfo(cardInfo)
                .buttons(buttons)
                .build();
    }
}
