// src/main/java/com/azure/service/clova/ClovaSpeechService.java
package com.azure.service.clova;

import com.azure.config.ClovaProps;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClovaSpeechService {
    private final ClovaProps props;
    private final RestTemplate rt = new RestTemplate();

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("X-NCP-APIGW-API-KEY-ID", props.getKeyId());
        h.set("X-NCP-APIGW-API-KEY", props.getKeySecret());
        return h;
    }

    /** meetingId를 callback 쿼리스트링으로 넘겨 콜백에서 식별 가능하게 */
    public SubmitResp submitForMeeting(Long meetingId, String audioUrl, boolean diarization) {
        String cb = props.getCallbackUrl();
        if (StringUtils.hasText(cb)) cb = cb + "?meetingId=" + meetingId;
        Map<String,Object> body = Map.of(
                "url", audioUrl, "language", "ko-KR", "completion", "async",
                "enable_diary", diarization, "callback", cb
        );
        return rt.exchange(props.getEndpoint(), HttpMethod.POST,
                new HttpEntity<>(body, headers()), SubmitResp.class).getBody();
    }

    @Data public static class SubmitResp { private String jobId; private String status; }
}
