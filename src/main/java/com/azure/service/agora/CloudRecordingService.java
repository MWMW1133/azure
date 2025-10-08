// src/main/java/com/azure/service/agora/CloudRecordingService.java
package com.azure.service.agora;

import com.azure.config.AgoraProps;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudRecordingService {
    private final AgoraProps props;
    private final RestTemplate rt = new RestTemplate();

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        String basic = props.getCustomerId() + ":" + props.getCustomerSecret();
        String encoded = Base64.getEncoder().encodeToString(basic.getBytes(StandardCharsets.UTF_8));
        h.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        return h;
    }

    public AcquireResp acquire(String channel, String uid) {
        String url = "https://api.agora.io/v1/apps/" + props.getAppId() + "/cloud_recording/acquire";
        Map<String,Object> body = Map.of("cname", channel, "uid", uid, "clientRequest", Map.of("resourceExpiredHour", 24));
        return rt.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers()), AcquireResp.class).getBody();
    }

    public StartResp start(String channel, String uid, String resourceId, long eventId) {
        String url = "https://api.agora.io/v1/apps/" + props.getAppId()
                + "/cloud_recording/resourceid/" + resourceId + "/mode/mix/start";

        String prefix = props.getBucket().getFilePrefix().replace("{eventId}", String.valueOf(eventId));
        Map<String,Object> storage = Map.of(
                "vendor", props.getBucket().getVendor(),
                "region", props.getBucket().getRegion(),
                "bucket", props.getBucket().getBucket(),
                "accessKey", props.getBucket().getAccessKey(),
                "secretKey", props.getBucket().getSecretKey(),
                "fileNamePrefix", new String[]{ prefix }
        );
        Map<String,Object> rec = Map.of("maxIdleTime", 60, "streamTypes", 0, "audioProfile", 1, "channelType", 0);
        Map<String,Object> body = Map.of("cname", channel, "uid", uid, "clientRequest", Map.of("recordingConfig", rec, "storageConfig", storage));
        return rt.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers()), StartResp.class).getBody();
    }

    public StopResp stop(String channel, String uid, String resourceId, String sid) {
        String url = "https://api.agora.io/v1/apps/" + props.getAppId()
                + "/cloud_recording/resourceid/" + resourceId + "/sid/" + sid + "/mode/mix/stop";
        Map<String,Object> body = Map.of("cname", channel, "uid", uid, "clientRequest", Map.of());
        return rt.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers()), StopResp.class).getBody();
    }

    @Data public static class AcquireResp { private String resourceId; }
    @Data public static class StartResp { private String resourceId; private String sid; }
    @Data public static class StopResp {
        private String resourceId; private String sid; private ServerResponse serverResponse;
        @Data public static class ServerResponse { private String fileList; }
    }
}
