package com.azure.controller.agora;

import com.azure.config.AgoraProps;
import com.azure.service.agora.AgoraTokenService;
import lombok.*;
import org.springframework.web.bind.annotation.*;

// src/main/java/com/azure/controller/api/RtcTokenController.java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rtc")
public class RtcTokenController {
    private final AgoraTokenService tokenService;
    private final AgoraProps props;

    @PostMapping("/token")
    public TokenResp token(@RequestBody TokenReq r) {
        String t = tokenService.buildToken(r.channel, r.uid, props.getTokenTtlSeconds());
        // 👇 appId도 함께 내려줌 (JSP 수정 불필요)
        return new TokenResp(t, props.getAppId());
    }

    @Getter @Setter public static class TokenReq { public String channel; public String uid; }
    // 👇 token + appId
    public record TokenResp(String token, String appId) {}
}
