package com.chat.chart.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai-service")
public class AiServiceProperties {

    private String url = "http://aiml-pub.aisp.test.abc/agent-api/workflow-agent-1-a852be77";

    private String healthCheckApi = "/chatabc/health_check";

    private String initSessionApi = "/chatabc/init_session";

    private String uploadFileApi = "/chatabc/upload_file";

    private String chatApi = "/chatabc/chat";

    private String downloadApi = "/chatabc/download_file";

    private long timeout = 90000L;
}
