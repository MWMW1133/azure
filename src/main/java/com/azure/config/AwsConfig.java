// src/main/java/com/azure/config/AwsConfig.java
package com.azure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.transcribe.TranscribeClient;

@Configuration
@RequiredArgsConstructor
public class AwsConfig {

    @Bean
    Region awsRegion(@Value("${cloud.aws.region}") String region) {
        Region r = Region.of(region);
        org.slf4j.LoggerFactory.getLogger(AwsConfig.class)
                .info("AWS region = {}", r.id());
        return r;
    }

    @Bean
    AwsCredentialsProvider awsCredentialsProvider(
            @Value("${cloud.aws.credentials.access-key:}") String accessKey,
            @Value("${cloud.aws.credentials.secret-key:}") String secretKey
    ) {
        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            );
        }
        return DefaultCredentialsProvider.create();
    }

    @Bean
    S3Client s3Client(Region region, AwsCredentialsProvider creds) {
        return S3Client.builder()
                .region(region)
                .credentialsProvider(creds)
                .build();
    }

    @Bean
    S3Presigner s3Presigner(Region region, AwsCredentialsProvider creds) {
        return S3Presigner.builder()
                .region(region)
                .credentialsProvider(creds)
                .build();
    }

    @Bean
    TranscribeClient transcribeClient(Region region, AwsCredentialsProvider creds) {
        return TranscribeClient.builder()
                .region(region)
                .credentialsProvider(creds)
                .build();
    }
}
