// src/main/java/com/azure/service/agora/AgoraTokenService.java
package com.azure.service.agora;

import com.azure.config.AgoraProps;
import io.agora.media.RtcTokenBuilder2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgoraTokenService {
    private final AgoraProps props;

    public String buildToken(String channel, String uid, int expireSec) {
        int tokenExpire = expireSec, privilegeExpire = expireSec;
        return new RtcTokenBuilder2().buildTokenWithUserAccount(
                props.getAppId(), props.getAppCertificate(), channel, uid,
                RtcTokenBuilder2.Role.ROLE_PUBLISHER, tokenExpire, privilegeExpire);
    }
}
