package com.azure.service.agora;

public interface CloudRecordingService {
    record StartResp(String resourceId, String sid, String channel) {}
    StartResp start(Long eventId, String uid);
    void stop(Long meetingId, String channel, String uid, String resourceId, String sid);
}
