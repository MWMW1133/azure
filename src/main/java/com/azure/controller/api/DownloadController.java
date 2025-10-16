package com.azure.controller.api;

import com.azure.config.S3Props;
import com.azure.config.TranscribeProps;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URI;
import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class DownloadController {

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final S3Props s3Props;          // 업로드 버킷
    private final TranscribeProps trProps;  // 결과 버킷 (폴백용)

    @GetMapping("/download/transcript")
    public ResponseEntity<?> downloadTranscript(@RequestParam String s3Key) {
        // 1) 업로드 버킷 우선
        if (exists(s3Props.getBucket(), s3Key)) {
            return redirectPresigned(s3Props.getBucket(), s3Key);
        }
        // 2) 업로드 버킷에 없다면 결과 버킷(Transcribe 결과) 폴백
        if (trProps.getResultsBucket() != null && !trProps.getResultsBucket().isBlank()) {
            if (exists(trProps.getResultsBucket(), s3Key)) {
                return redirectPresigned(trProps.getResultsBucket(), s3Key);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("아직 전사가 완료되지 않았거나 키가 없습니다: " + s3Key);
    }

    private boolean exists(String bucket, String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket).key(key).build());
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

    private ResponseEntity<?> redirectPresigned(String bucket, String key) {
        var pre = presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build())
                .build());
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(pre.url().toString()))
                .build();
    }
}
