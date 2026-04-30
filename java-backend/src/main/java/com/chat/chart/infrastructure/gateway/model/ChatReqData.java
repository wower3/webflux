package com.chat.chart.infrastructure.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聊天请求数据
 * <p>
 * 封装发送给AI服务的聊天请求业务数据，
 * 包含会话ID、消息文本、流式开关及附件信息。
 * </p>
 *
 * @author Chat Chart System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatReqData {

    /** 会话ID，用于关联上下文 */
    @JsonProperty("session_id")
    private String sessionId;

    /** 用户消息文本 */
    @JsonProperty("txt")
    private String txt;

    /** 是否启用流式响应："true" / "false" */
    @JsonProperty("stream")
    private String stream;

    /** 附件文件列表 */
    @JsonProperty("files")
    private List<FileData> files;

    /**
     * 文件附件数据
     * <p>
     * 描述上传的文件信息，包含文件ID、访问URL和内容类型。
     * </p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileData {

        /** 文件唯一标识 */
        @JsonProperty("fileId")
        private String fileId;

        /** 文件访问URL */
        @JsonProperty("url")
        private String url;

        /** 文件内容类型（MIME） */
        @JsonProperty("contentType")
        private String contentType;
    }
}
