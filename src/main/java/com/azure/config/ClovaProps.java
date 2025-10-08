// src/main/java/com/azure/config/ClovaProps.java
package com.azure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "clova")
public class ClovaProps {
    private String endpoint;
    private String keyId;
    private String keySecret;
    private String callbackUrl;
}
