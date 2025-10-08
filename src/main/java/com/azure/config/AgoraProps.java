// src/main/java/com/azure/config/AgoraProps.java
package com.azure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "agora")
public class AgoraProps {
    private String appId;
    private String appCertificate;
    private String customerId;
    private String customerSecret;
    private String publicBaseUrl;
    private Bucket bucket;

    @Data
    public static class Bucket {
        private Integer vendor;
        private String region;
        private String bucket;
        private String accessKey;
        private String secretKey;
        private String filePrefix; // recordings/event-{eventId}
    }
}
