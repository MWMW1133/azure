package com.azure.service.s3;

import com.azure.config.S3Props;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class S3UploadService {
    private final S3Props props;
    private final S3Presigner presigner;

    public S3UploadService(S3Props props) {
        this.props = props;
        var cred = AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey());
        this.presigner = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(cred))
                .region(Region.of(props.getRegion()))
                .build();
    }

    public PresignResp presignPut(String key, String contentType) {
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
        String publicUrl = props.getPublicBaseUrl() != null ? props.getPublicBaseUrl() + "/" + key : null;
        return new PresignResp(pre.url().toString(), pre.signedHeaders(), publicUrl);
    }

    public record PresignResp(String url, Map<String, List<String>> headers, String publicUrl) {}
}
