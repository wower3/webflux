package com.chat.chart.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话数据对象
 */
@Data
@TableName("conversation")
public class ConversationDO {

    private String conversationId;

    private String userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
