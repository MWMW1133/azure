package com.azure.controller.s3;

import com.azure.service.s3.S3UploadService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads")
public class UploadController {
    private final S3UploadService s3;

    @PostMapping("/presign")
    public Resp presign(@RequestBody Req r) {
        String key = r.objectKey;
        if (key == null || key.isBlank()) {
            key = "meetings/%d/%d.webm".formatted(r.meetingId, System.currentTimeMillis());
        }
        var p = s3.presignPut(key, r.contentType != null ? r.contentType : "video/webm");
        return new Resp(key, p.url(), p.headers(), p.publicUrl());
    }

    @Data public static class Req { private Long meetingId; private String objectKey; private String contentType; }
    public record Resp(String objectKey, String url, Map<String, List<String>> headers, String publicUrl) {}
}
