package com.azure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aws.s3")
public class S3Props {
    private String accessKey;
    private String secretKey;
    private String region = "ap-northeast-2";
    private String bucket;
    private String publicBaseUrl;      // 예: https://your-bucket.s3.ap-northeast-2.amazonaws.com
    private int presignExpirySeconds = 900;
}
