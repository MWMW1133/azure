package com.azure.controller.agora;

import com.azure.config.AgoraProps;
import com.azure.service.agora.AgoraTokenService;
import lombok.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rtc")
public class RtcTokenController {
    private final AgoraTokenService tokenService;
    private final AgoraProps props;

    @PostMapping("/token")
    public TokenResp token(@RequestBody TokenReq r) {
        String t = tokenService.buildToken(r.channel, r.uid, props.getTokenTtlSeconds());
        return new TokenResp(t);
    }

    @Getter @Setter public static class TokenReq { public String channel; public String uid; }
    @AllArgsConstructor @Getter public static class TokenResp { private String token; }
}
