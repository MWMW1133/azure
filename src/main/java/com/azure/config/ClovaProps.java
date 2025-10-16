package com.azure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "clova")
public class ClovaProps {
    private String clientId;      // X-NCP-APIGW-API-KEY-ID
    private String clientSecret;  // X-NCP-APIGW-API-KEY
    private String speechUrl;     // Invoke URL (REST)
    private String callbackUrl;   // 콜백 받을 우리 서버 URL
}
