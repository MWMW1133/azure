package com.azure.service.s3;

import com.azure.config.S3Props;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client; // ✅ S3Client 임포트
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Props props;
    private final S3Client s3Client;       // ✅ S3Client Bean 주입
    private final S3Presigner s3Presigner; // ✅ S3Presigner Bean 주입

    // ❌ 아래 클라이언트를 직접 만들던 메소드들은 모두 삭제합니다.
    /*
    private AwsCredentialsProvider creds() { ... }
    private S3Client client() { ... }
    private S3Presigner presigner() { ... }
    */

    /** Public/Virtual-hosted/S3 URL → Object Key */
    public String deriveKeyFromPublicUrl(String audioUrl) {
        String base = props.getPublicBaseUrl();
        if (base != null && !base.isBlank() && audioUrl.startsWith(base)) {
            return audioUrl.substring(base.length()).replaceFirst("^/", "");
        }
        String marker = ".amazonaws.com/";
        int i = audioUrl.indexOf(marker);
        if (i > 0) {
            return audioUrl.substring(i + marker.length());
        }
        return audioUrl.replaceFirst("^https?://[^/]+/", "");
    }

    /** 파일을 안전하게 공유하기 위한 Presigned GET URL 생성 */
    public String presignGetUrl(String key, Duration expiry) {
        // ✅ 주입받은 s3Presigner를 직접 사용하도록 변경
        PresignedGetObjectRequest p = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(expiry)
                        .getObjectRequest(r -> r.bucket(props.getBucket()).key(key))
                        .build()
        );
        return p.url().toString();
    }
}