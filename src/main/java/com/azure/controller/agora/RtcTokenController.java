// src/main/java/com/azure/controller/agora/RtcTokenController.java
package com.azure.controller.agora;

import com.azure.service.agora.AgoraTokenService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rtc")
public class RtcTokenController {
    private final AgoraTokenService tokenService;

    @PostMapping("/token")
    public TokenResp token(@RequestBody TokenReq r) {
        return new TokenResp(tokenService.buildToken(r.channel, r.uid, 3600));
    }

    @Data @NoArgsConstructor public static class TokenReq { private String channel; private String uid; }
    @Data @AllArgsConstructor public static class TokenResp { private String token; }
}
