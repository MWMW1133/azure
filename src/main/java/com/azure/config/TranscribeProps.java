package com.azure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Transcribe 결과가 생성되는 버킷/프리픽스 설정
 * application.properties:
 *   transcribe.results-bucket=${AWS_TRANSCRIBE_RESULTS_BUCKET}
 *   transcribe.output-prefix=${AWS_TRANSCRIBE_OUTPUT_PREFIX:}
 */
@Data
@Component
@ConfigurationProperties(prefix = "transcribe")
public class TranscribeProps {
    private String resultsBucket;
    private String outputPrefix = ""; // 필요 없으면 빈 값
}
