package com.azure.controller.meeting;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.agora.media.RtcTokenBuilder;
import io.agora.media.RtcTokenBuilder.Role;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/agora")
public class AgoraTokenController {

    // 실제로는 application.properties나 yml 파일에서 관리해야 합니다.
    private final String appId = "YOUR_AGORA_APP_ID"; // 아고라 콘솔에서 발급받은 App ID
    private final String appCertificate = "YOUR_AGORA_APP_CERTIFICATE"; // 아고라 콘솔에서 발급받은 App Certificate

    @GetMapping("/token")
    public Map<String, String> getAgoraToken(@RequestParam String channelName) {

        RtcTokenBuilder tokenBuilder = new RtcTokenBuilder();

        // 토큰 만료 시간 (현재로부터 1시간 뒤)
        int expirationTimeInSeconds = 3600;
        int timestamp = (int) (System.currentTimeMillis() / 1000 + expirationTimeInSeconds);

        // 토큰 생성 (사용자 ID는 임의로 0으로 설정, 실제 서비스에서는 회원 ID 사용)
        String token = tokenBuilder.buildTokenWithUid(appId, appCertificate, channelName, 0, Role.Role_Publisher, timestamp);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);

        return tokenMap;
    }
}