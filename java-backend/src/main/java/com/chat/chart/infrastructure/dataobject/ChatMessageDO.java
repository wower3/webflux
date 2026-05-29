package com.chat.chart.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息数据对象
 */
@Data
@TableName("chat_message")
public class ChatMessageDO {

    private String requestId;

    private String conversationId;

    private String role;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private String adoptionStatus;

    private String isSuccess;
}
