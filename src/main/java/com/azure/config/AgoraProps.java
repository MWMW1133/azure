package com.azure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// src/main/java/com/azure/config/AgoraProps.java
@Data
@Component
@ConfigurationProperties(prefix = "agora")
public class AgoraProps {
    private String appId;
    private String appCertificate;
    private String customerId;
    private String customerSecret;
    private int tokenTtlSeconds = 3600;

    // Cloud Recording 저장소 설정
    private Integer bucketVendor;     // 1 = S3
    private Integer bucketRegion;     // 11 = ap-northeast-2 (서울)
    private String  bucketBucket;
    private String  bucketAccessKey;
    private String  bucketSecretKey;
    private String  bucketFilePrefix;

    // 녹음 파일 공개(또는 CDN) 베이스 URL (옵션)
    private String  publicBaseUrl;    // 예: https://your-bucket.s3.ap-northeast-2.amazonaws.com
}
