package com.azure.service.s3;

import com.azure.config.S3Props;
import lombok.RequiredArgsConstructor; // ✅ RequiredArgsConstructor 사용
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor // ✅ Lombok 어노테이션으로 생성자 자동 생성
public class S3UploadService {

    private final S3Props props;
    private final S3Presigner presigner; // ✅ AwsConfig에 등록된 Bean을 주입받음

    /* ❌ 아래 생성자는 @RequiredArgsConstructor가 대신하므로 삭제합니다.
    public S3UploadService(S3Props props) {
        this.props = props;
        var cred = AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey());
        this.presigner = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(cred))
                .region(Region.of(props.getRegion()))
                .build();
    }
    */

    public PresignResp presignPut(String key, String contentType) {
        // 이 메소드의 내용은 변경할 필요 없이 그대로 둡니다.
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest req = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(props.getPresignExpirySeconds()))
                .putObjectRequest(put)
                .build();

        PresignedPutObjectRequest pre = presigner.presignPutObject(req);
        // publicBaseUrl이 application.properties에 aws.s3.public-base-url로 정의되어 있어야 합니다.
        String publicUrl = props.getPublicBaseUrl() != null ? props.getPublicBaseUrl() + "/" + key : null;
        return new PresignResp(pre.url().toString(), pre.signedHeaders(), publicUrl);
    }

    public record PresignResp(String url, Map<String, List<String>> headers, String publicUrl) {}
}