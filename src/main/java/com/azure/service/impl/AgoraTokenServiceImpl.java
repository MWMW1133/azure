package com.azure.service.impl;

import com.azure.config.AgoraProps;
import io.agora.media.RtcTokenBuilder;
import io.agora.media.RtcTokenBuilder.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.azure.service.agora.AgoraTokenService;

@Service
@RequiredArgsConstructor
public class AgoraTokenServiceImpl implements AgoraTokenService {
    private final AgoraProps props;

    @Override
    public String buildToken(String channel, String uid, int ttlSeconds) {
        int expireTs = (int)(System.currentTimeMillis()/1000 + ttlSeconds);
        int uidInt = 0;
        try { uidInt = Integer.parseInt(uid); } catch (Exception ignore) {}
        return new RtcTokenBuilder().buildTokenWithUid(
                props.getAppId(), props.getAppCertificate(),
                channel, uidInt, Role.Role_Publisher, expireTs
        );
    }
}
