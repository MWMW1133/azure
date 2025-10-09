package com.azure.service.impl;

import com.azure.config.AgoraProps;
import com.azure.model.file.FileObject;
import com.azure.model.meeting.Meeting;
import com.azure.repository.FileObjectRepository;
import com.azure.repository.MeetingRepository;
import com.azure.service.CloudRecordingService;
import com.azure.service.clova.ClovaSpeechService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class CloudRecordingServiceImpl implements CloudRecordingService {

    private final AgoraProps props;
    private final RestTemplate rt = new RestTemplate();

    private final MeetingRepository meetingRepository;
    private final FileObjectRepository fileRepo;
    private final ClovaSpeechService clova;

    private String authHeader() {
        String raw = props.getCustomerId() + ":" + props.getCustomerSecret();
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes());
    }
    private String channelNameOf(Long eventId){ return "meeting-" + eventId; }

    @Override
    public StartResp start(Long eventId, String uid) {
        String channel = channelNameOf(eventId);

        // 1) acquire
        String acquireUrl = "https://api.agora.io/v1/apps/" + props.getAppId()
                + "/cloud_recording/acquire";
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", authHeader());
        h.setContentType(MediaType.APPLICATION_JSON);

        Map<String,Object> acquireBody = Map.of(
                "cname", channel,
                "uid", uid,
                "clientRequest", Map.of("resourceExpiredHour", 24)
        );
        Map acq = rt.exchange(acquireUrl, HttpMethod.POST,
                new HttpEntity<>(acquireBody, h), Map.class).getBody();
        String resourceId = String.valueOf(acq.get("resourceId"));

        // 2) start
        String base = props.getBucketFilePrefix().replace("{eventId}", String.valueOf(eventId));
        String[] fileNamePrefix = base.replaceFirst("^/+", "").split("/+");

        String startUrl = "https://api.agora.io/v1/apps/" + props.getAppId()
                + "/cloud_recording/resourceid/" + resourceId + "/mode/mix/start";

        Map<String,Object> startBody = Map.of(
                "cname", channel,
                "uid", uid,
                "clientRequest", Map.of(
                        "recordingConfig", Map.of(
                                "channelType", 0,
                                "streamTypes", 0, // audio only
                                "audioProfile", 1
                        ),
                        "recordingFileConfig", Map.of("avFileType", new String[]{"hls","mp4"}),
                        "storageConfig", Map.of(
                                "vendor", props.getBucketVendor(),
                                "region", props.getBucketRegion(),
                                "bucket", props.getBucketBucket(),
                                "accessKey", props.getBucketAccessKey(),
                                "secretKey", props.getBucketSecretKey(),
                                "fileNamePrefix", fileNamePrefix
                        )
                )
        );
        Map st = rt.exchange(startUrl, HttpMethod.POST,
                new HttpEntity<>(startBody, h), Map.class).getBody();
        String sid = String.valueOf(st.get("sid"));

        return new StartResp(resourceId, sid, channel);
    }

    @Override
    public void stop(Long meetingId, String channel, String uid, String resourceId, String sid) {
        String stopUrl = "https://api.agora.io/v1/apps/" + props.getAppId()
                + "/cloud_recording/resourceid/" + resourceId + "/sid/" + sid + "/mode/mix/stop";

        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", authHeader());
        h.setContentType(MediaType.APPLICATION_JSON);

        Map<String,Object> stopBody = Map.of("cname", channel, "uid", uid, "clientRequest", Map.of());
        Map res = rt.exchange(stopUrl, HttpMethod.POST,
                new HttpEntity<>(stopBody, h), Map.class).getBody();

        // === fileList 파싱 (첫 파일 기준) ===
        String storageKey = null;
        Long   size = 0L;
        try {
            Map<String,Object> serverResponse = (Map<String,Object>) res.get("serverResponse");
            List<Map<String,Object>> fileList = (List<Map<String,Object>>) serverResponse.get("fileList");
            if (fileList != null && !fileList.isEmpty()) {
                // Agora는 업로드된 오브젝트 키 전체 경로를 fileName으로 줌
                String fileName = String.valueOf(fileList.get(0).get("fileName")); // recordings/event-xx/xxx.m3u8 or .mp4
                storageKey = fileName.replaceFirst("^/+", "");
                Object fs = fileList.get(0).get("fileSize");
                if (fs != null) size = Long.parseLong(String.valueOf(fs));
            }
        } catch (Exception ignore) {}

        // === DB 저장 ===
        Meeting m = meetingRepository.findById(meetingId).orElseThrow();

        FileObject f = new FileObject();
        f.setOrganization(m.getOrganization()); // NOT NULL
        f.setFileName(storageKey != null && storageKey.contains("/") ? storageKey.substring(storageKey.lastIndexOf('/')+1) : ("recording-" + sid + ".mp4"));
        f.setMimeType("audio/mp4"); // 필요시 조정
        f.setSize(size != null ? size : 0L);
        f.setStorageKey(storageKey != null ? storageKey
                : (props.getBucketFilePrefix().replace("{eventId}", String.valueOf(m.getEvent().getId()))
                + "/recording-" + sid + ".mp4"));
        fileRepo.save(f);

        m.setRecordingFile(f);
        meetingRepository.save(m);

        // === Clova 제출용 파일 URL 생성 ===
        String audioUrl = buildPlayableUrl(f.getStorageKey());
        try { clova.submitForMeeting(meetingId, audioUrl, true); } catch (Exception ignore) {}
    }

    private String buildPlayableUrl(String storageKey){
        String key = storageKey.replaceFirst("^/+", "");
        // 1) 설정된 publicBaseUrl 최우선
        if (props.getPublicBaseUrl() != null && !props.getPublicBaseUrl().isBlank()){
            return props.getPublicBaseUrl().replaceAll("/+$","") + "/" + key;
        }
        // 2) 퍼블릭 S3 URL 조립 (서울 고정 또는 매핑)
        String region = (props.getBucketRegion()!=null && props.getBucketRegion()==11) ? "ap-northeast-2" : "ap-northeast-2";
        return "https://" + props.getBucketBucket() + ".s3." + region + ".amazonaws.com/" + key;
    }
}
