package com.chat.chart.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatReqData {

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("txt")
    private String txt;

    @JsonProperty("stream")
    private String stream;

    @JsonProperty("files")
    private List<FileData> files;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileData {

        @JsonProperty("fileId")
        private String fileId;

        @JsonProperty("url")
        private String url;

        @JsonProperty("contentType")
        private String contentType;
    }
}
