package com.azure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aws.s3")
public class S3Props {
    // ❌ Access Key와 Secret Key는 Spring Boot 자동 설정에 맡기므로 삭제합니다.
    // private String accessKey;
    // private String secretKey;

    // ✅ 아래 설정들은 계속 사용하므로 그대로 둡니다.
    private String region = "ap-northeast-2";
    private String bucket;
    private String publicBaseUrl;
    private int presignExpirySeconds = 900;
}