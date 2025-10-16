package com.azure.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RestController
public class AudioUploadController {
    private final S3Client s3Client;
    private final String bucketName = "your-project-audio-uploads"; // 음성 업로드용 버킷

    public AudioUploadController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @PostMapping("/upload/audio")
    public ResponseEntity<String> uploadAudio(@RequestParam("audioFile") MultipartFile file) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(file.getOriginalFilename())
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return ResponseEntity.ok("파일 업로드 성공: " + file.getOriginalFilename());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("파일 업로드 실패: " + e.getMessage());
        }
    }
}