package com.azure.service.agora;

public interface AgoraTokenService {
    String buildToken(String channel, String uid, int ttlSeconds);
}
