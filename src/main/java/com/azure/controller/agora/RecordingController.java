// src/main/java/com/azure/controller/agora/RecordingController.java
package com.azure.controller.agora;

import com.azure.service.agora.CloudRecordingService;
import com.azure.service.meeting.RecordingFinalizeService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recordings")
public class RecordingController {

    private final CloudRecordingService rec;
    private final RecordingFinalizeService finalizeSvc;

    @PostMapping("/start")
    public StartResp start(@RequestBody StartReq r) {
        String channel = "event-" + r.eventId;
        var acq = rec.acquire(channel, r.uid);
        var st  = rec.start(channel, r.uid, acq.getResourceId(), r.eventId);
        return new StartResp(acq.getResourceId(), st.getSid(), channel);
    }

    @PostMapping("/stop")
    public StopResp stop(@RequestBody StopReq r) {
        var st = rec.stop(r.channel, r.uid, r.resourceId, r.sid);
        String fileList = (st.getServerResponse() != null) ? st.getServerResponse().getFileList() : "[]";
        finalizeSvc.handleStopAndSubmit(r.meetingId, fileList);
        return new StopResp("ok");
    }

    // ==== DTOs ====
    @Data @NoArgsConstructor public static class StartReq { private Long eventId; private String uid; }
    @Data @AllArgsConstructor public static class StartResp { private String resourceId; private String sid; private String channel; }
    @Data @NoArgsConstructor public static class StopReq { private Long meetingId; private String channel; private String uid; private String resourceId; private String sid; }
    @Data @AllArgsConstructor public static class StopResp { private String status; }
}
