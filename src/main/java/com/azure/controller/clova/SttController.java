package com.azure.controller.clova;

import lombok.Data;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meet/stt")
public class SttController {

    @PostMapping("/start")
    public Resp start(@RequestBody SttReq r) {
        // TODO: Clova 실시간 STT 세션 열기 or 파일 기반 처리 예약
        return new Resp(true);
    }

    @PostMapping("/stop")
    public Resp stop(@RequestBody SttReq r) {
        // TODO: 세션 종료/마무리
        return new Resp(true);
    }

    @Data public static class SttReq { private Long meetingId; }
    public record Resp(boolean ok) { }
}
